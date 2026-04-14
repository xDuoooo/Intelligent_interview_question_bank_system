package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.PageRequest;
import com.xduo.springbootinit.constant.CommonConstant;
import com.xduo.springbootinit.constant.PostConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.mapper.PostFavourMapper;
import com.xduo.springbootinit.mapper.PostMapper;
import com.xduo.springbootinit.mapper.PostThumbMapper;
import com.xduo.springbootinit.esdao.PostEsDao;
import com.xduo.springbootinit.model.dto.post.PostEsDTO;
import com.xduo.springbootinit.model.dto.post.PostQueryRequest;
import com.xduo.springbootinit.model.entity.*;
import com.xduo.springbootinit.model.vo.PostVO;
import com.xduo.springbootinit.model.vo.UserVO;
import com.xduo.springbootinit.service.PostService;
import com.xduo.springbootinit.service.EsSyncTaskService;
import com.xduo.springbootinit.service.UserService;
import com.xduo.springbootinit.utils.SqlUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.collection.CollUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

/**
 * 帖子服务实现
 *
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Resource
    private PostEsDao postEsDao;

    @Resource
    private EsSyncTaskService esSyncTaskService;

    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        String tags = post.getTags();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param postQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        Integer reviewStatus = postQueryRequest.getReviewStatus();
        Integer isTop = postQueryRequest.getIsTop();
        Integer isFeatured = postQueryRequest.getIsFeatured();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        if (reviewStatus != null && PostConstant.ALLOWED_REVIEW_STATUS_SET.contains(reviewStatus)) {
            if (PostConstant.REVIEW_STATUS_APPROVED == reviewStatus) {
                queryWrapper.and(qw -> qw.eq("reviewStatus", reviewStatus).or().isNull("reviewStatus"));
            } else {
                queryWrapper.eq("reviewStatus", reviewStatus);
            }
        }
        queryWrapper.eq(isTop != null, "isTop", isTop);
        queryWrapper.eq(isFeatured != null, "isFeatured", isFeatured);
        queryWrapper.orderByDesc("isTop", "isFeatured");
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<Post> searchFromEs(PostQueryRequest postQueryRequest) {
        Long id = postQueryRequest.getId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        List<String> orTagList = postQueryRequest.getOrTags();
        Long userId = postQueryRequest.getUserId();
        Integer reviewStatus = postQueryRequest.getReviewStatus();
        Integer isTop = postQueryRequest.getIsTop();
        Integer isFeatured = postQueryRequest.getIsFeatured();
        long requestedCurrent = Math.max(postQueryRequest.getCurrent(), 1);
        long current = requestedCurrent - 1;
        long pageSize = Math.max(postQueryRequest.getPageSize(), 1);
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 过滤
        boolQueryBuilder.filter(f -> f.term(t -> t.field("isDelete").value(0)));
        if (reviewStatus != null && PostConstant.ALLOWED_REVIEW_STATUS_SET.contains(reviewStatus)) {
            if (PostConstant.REVIEW_STATUS_APPROVED == reviewStatus) {
                boolQueryBuilder.filter(f -> f.bool(b -> b
                        .should(s -> s.term(t -> t.field("reviewStatus").value(reviewStatus)))
                        .should(s -> s.bool(bb -> bb.mustNot(m -> m.exists(e -> e.field("reviewStatus")))))
                        .minimumShouldMatch("1")));
            } else {
                boolQueryBuilder.filter(f -> f.term(t -> t.field("reviewStatus").value(reviewStatus)));
            }
        }
        if (id != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("id").value(id)));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(m -> m.term(t -> t.field("id").value(notId)));
        }
        if (userId != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("userId").value(userId)));
        }
        if (isTop != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("isTop").value(isTop)));
        }
        if (isFeatured != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("isFeatured").value(isFeatured)));
        }
        // 必须包含所有标签
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                boolQueryBuilder.filter(f -> f.term(t -> t.field("tags").value(tag)));
            }
        }
        // 包含任何一个标签即可
        if (CollUtil.isNotEmpty(orTagList)) {
            boolQueryBuilder.filter(f -> f.bool(b -> {
                for (String tag : orTagList) {
                    b.should(s -> s.term(t -> t.field("tags").value(tag)));
                }
                return b.minimumShouldMatch("1");
            }));
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(s -> s.match(m -> m.field("title").query(searchText)));
            boolQueryBuilder.should(s -> s.match(m -> m.field("description").query(searchText)));
            boolQueryBuilder.should(s -> s.match(m -> m.field("content").query(searchText)));
            boolQueryBuilder.minimumShouldMatch("1");
        }
        // 按标题检索
        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.should(s -> s.match(m -> m.field("title").query(title)));
            boolQueryBuilder.minimumShouldMatch("1");
        }
        // 按内容检索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(s -> s.match(m -> m.field("content").query(content)));
            boolQueryBuilder.minimumShouldMatch("1");
        }

        // 排序
        SortOptions sortOptions;
        if (StringUtils.isNotBlank(sortField)) {
            sortOptions = SortOptions.of(s -> s.field(f -> f
                    .field(sortField)
                    .order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.Asc : SortOrder.Desc)));
        } else {
            sortOptions = SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc)));
        }

        // 构造 NativeQuery
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .withSort(sortOptions)
                .withPageable(org.springframework.data.domain.PageRequest.of((int) current, (int) pageSize))
                .build();

        Page<Post> page = new Page<>(requestedCurrent, pageSize);
        List<Post> resourceList = new ArrayList<>();
        try {
            SearchHits<PostEsDTO> searchHits = elasticsearchOperations.search(nativeQuery, PostEsDTO.class);
            page.setTotal(searchHits.getTotalHits());

            if (searchHits.hasSearchHits()) {
                List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();
                List<Long> postIdList = searchHitList.stream()
                        .map(hit -> hit.getContent().getId())
                        .collect(Collectors.toList());

                if (CollUtil.isNotEmpty(postIdList)) {
                    List<Post> postList = baseMapper.selectBatchIds(postIdList);
                    if (postList != null) {
                        Map<Long, List<Post>> idPostMap = postList.stream().collect(Collectors.groupingBy(Post::getId));
                        postIdList.forEach(postId -> {
                            if (idPostMap.containsKey(postId)) {
                                resourceList.add(idPostMap.get(postId).get(0));
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            log.error("es search error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询 ES 失败");
        }

        page.setRecords(resourceList);
        return page;
    }

    @Override
    public void syncPostToEs(Post post) {
        if (post == null || post.getId() == null) {
            return;
        }
        try {
            PostEsDTO postEsDTO = PostEsDTO.objToDto(post);
            postEsDao.save(postEsDTO);
            esSyncTaskService.clearTask("post", post.getId());
        } catch (Exception e) {
            log.error("sync post to es error, postId={}", post.getId(), e);
            esSyncTaskService.recordUpsertFailure("post", post.getId(),
                    cn.hutool.json.JSONUtil.toJsonStr(PostEsDTO.objToDto(post)), e);
        }
    }

    @Override
    public void deletePostFromEs(Long postId) {
        if (postId == null || postId <= 0) {
            return;
        }
        try {
            postEsDao.deleteById(postId);
            esSyncTaskService.clearTask("post", postId);
        } catch (Exception e) {
            log.error("delete post from es error, postId={}", postId, e);
            esSyncTaskService.recordDeleteFailure("post", postId, e);
        }
    }

    @Override
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        PostVO postVO = PostVO.objToVo(post);
        long postId = post.getId();
        // 1. 关联查询用户信息
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postId);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            PostThumb postThumb = postThumbMapper.selectOne(postThumbQueryWrapper);
            postVO.setHasThumb(postThumb != null);
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postId);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            PostFavour postFavour = postFavourMapper.selectOne(postFavourQueryWrapper);
            postVO.setHasFavour(postFavour != null);
        }
        return postVO;
    }

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollUtil.isEmpty(postList)) {
            return postVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postList.stream()
                .map(Post::getUserId)
                .filter(userId -> userId != null && userId > 0)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userIdSet.isEmpty()
                ? java.util.Collections.emptyMap()
                : userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> postIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> postIdSet = postList.stream()
                    .map(Post::getId)
                    .filter(postId -> postId != null && postId > 0)
                    .collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            if (CollUtil.isNotEmpty(postIdSet)) {
                QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
                postThumbQueryWrapper.in("postId", postIdSet);
                postThumbQueryWrapper.eq("userId", loginUser.getId());
                List<PostThumb> postPostThumbList = postThumbMapper.selectList(postThumbQueryWrapper);
                postPostThumbList.forEach(postPostThumb -> postIdHasThumbMap.put(postPostThumb.getPostId(), true));
                // 获取收藏
                QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
                postFavourQueryWrapper.in("postId", postIdSet);
                postFavourQueryWrapper.eq("userId", loginUser.getId());
                List<PostFavour> postFavourList = postFavourMapper.selectList(postFavourQueryWrapper);
                postFavourList.forEach(postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true));
            }
        }
        // 填充信息
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            postVO.setHasThumb(postIdHasThumbMap.getOrDefault(post.getId(), false));
            postVO.setHasFavour(postIdHasFavourMap.getOrDefault(post.getId(), false));
            return postVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }

    @Override
    public List<PostVO> listHotPostVO(int size, HttpServletRequest request) {
        int safeSize = Math.max(1, Math.min(size, 12));
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(qw -> qw.eq("reviewStatus", PostConstant.REVIEW_STATUS_APPROVED).or().isNull("reviewStatus"));
        queryWrapper.orderByDesc("thumbNum", "favourNum", "createTime");
        queryWrapper.last("limit " + safeSize * 3);
        List<Post> candidateList = this.list(queryWrapper);
        if (CollUtil.isEmpty(candidateList)) {
            return new ArrayList<>();
        }
        candidateList.sort((left, right) -> Double.compare(buildPostHotScore(right), buildPostHotScore(left)));
        List<Post> topList = candidateList.stream().limit(safeSize).collect(Collectors.toList());
        Page<Post> page = new Page<>(1, safeSize, topList.size());
        page.setRecords(topList);
        return getPostVOPage(page, request).getRecords();
    }

    @Override
    public List<PostVO> listFeaturedPostVO(int size, HttpServletRequest request) {
        int safeSize = Math.max(1, Math.min(size, 8));
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(qw -> qw.eq("reviewStatus", PostConstant.REVIEW_STATUS_APPROVED).or().isNull("reviewStatus"));
        queryWrapper.eq("isFeatured", 1);
        queryWrapper.orderByDesc("isTop", "thumbNum", "favourNum", "createTime");
        queryWrapper.last("limit " + Math.max(safeSize * 2, 8));
        List<Post> candidateList = this.list(queryWrapper);
        if (CollUtil.isEmpty(candidateList)) {
            return new ArrayList<>();
        }
        candidateList.sort((left, right) -> {
            double leftScore = buildPostHotScore(left) + (left.getIsTop() != null && left.getIsTop() == 1 ? 20 : 0);
            double rightScore = buildPostHotScore(right) + (right.getIsTop() != null && right.getIsTop() == 1 ? 20 : 0);
            return Double.compare(rightScore, leftScore);
        });
        List<Post> topList = candidateList.stream().limit(safeSize).collect(Collectors.toList());
        Page<Post> page = new Page<>(1, safeSize, topList.size());
        page.setRecords(topList);
        return getPostVOPage(page, request).getRecords();
    }

    @Override
    public List<PostVO> listRelatedPostVO(long postId, int size, HttpServletRequest request) {
        if (postId <= 0) {
            return new ArrayList<>();
        }
        Post currentPost = this.getById(postId);
        if (currentPost == null) {
            return new ArrayList<>();
        }
        List<String> currentTagList = PostVO.objToVo(currentPost).getTagList();
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("id", postId);
        queryWrapper.and(qw -> qw.eq("reviewStatus", PostConstant.REVIEW_STATUS_APPROVED).or().isNull("reviewStatus"));
        if (CollUtil.isNotEmpty(currentTagList)) {
            queryWrapper.and(qw -> {
                for (int i = 0; i < currentTagList.size(); i++) {
                    String tag = currentTagList.get(i);
                    if (i == 0) {
                        qw.like("tags", "\"" + tag + "\"");
                    } else {
                        qw.or().like("tags", "\"" + tag + "\"");
                    }
                }
            });
        }
        queryWrapper.orderByDesc("thumbNum", "favourNum", "createTime");
        queryWrapper.last("limit " + Math.max(size * 3, 12));
        List<Post> candidateList = this.list(queryWrapper);
        if (CollUtil.isEmpty(candidateList)) {
            return new ArrayList<>();
        }
        candidateList.sort((left, right) -> Double.compare(
                buildRelatedScore(currentTagList, right),
                buildRelatedScore(currentTagList, left)
        ));
        List<Post> topList = candidateList.stream()
                .filter(post -> buildRelatedScore(currentTagList, post) > 0)
                .limit(Math.max(1, Math.min(size, 8)))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(topList)) {
            topList = candidateList.stream().limit(Math.max(1, Math.min(size, 8))).collect(Collectors.toList());
        }
        Page<Post> page = new Page<>(1, topList.size(), topList.size());
        page.setRecords(topList);
        return getPostVOPage(page, request).getRecords();
    }

    private double buildPostHotScore(Post post) {
        if (post == null) {
            return 0;
        }
        long ageHours = Math.max(1L, (System.currentTimeMillis() - post.getCreateTime().getTime()) / (1000 * 60 * 60));
        double interactionScore = (post.getThumbNum() == null ? 0 : post.getThumbNum()) * 1.5
                + (post.getFavourNum() == null ? 0 : post.getFavourNum()) * 2.2;
        double freshnessBonus = 48.0 / Math.sqrt(ageHours + 1);
        return interactionScore + freshnessBonus;
    }

    private double buildRelatedScore(List<String> currentTagList, Post candidatePost) {
        if (candidatePost == null) {
            return 0;
        }
        List<String> candidateTagList = PostVO.objToVo(candidatePost).getTagList();
        long overlapCount = currentTagList == null ? 0 : currentTagList.stream()
                .filter(candidateTagList::contains)
                .count();
        return overlapCount * 10
                + (candidatePost.getThumbNum() == null ? 0 : candidatePost.getThumbNum()) * 0.8
                + (candidatePost.getFavourNum() == null ? 0 : candidatePost.getFavourNum()) * 1.1;
    }
}
