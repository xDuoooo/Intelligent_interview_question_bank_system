package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.constant.CommonConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.mapper.QuestionCommentLikeMapper;
import com.xduo.springbootinit.mapper.QuestionCommentMapper;
import com.xduo.springbootinit.mapper.QuestionCommentReportMapper;
import com.xduo.springbootinit.model.dto.comment.CommentAddRequest;
import com.xduo.springbootinit.model.dto.comment.CommentQueryRequest;
import com.xduo.springbootinit.model.dto.comment.CommentReportRequest;
import com.xduo.springbootinit.model.entity.QuestionComment;
import com.xduo.springbootinit.model.entity.QuestionCommentLike;
import com.xduo.springbootinit.model.entity.QuestionCommentReport;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.vo.CommentVO;
import com.xduo.springbootinit.model.vo.UserVO;
import com.xduo.springbootinit.service.QuestionCommentService;
import com.xduo.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目评论服务实现
 */
@Service
@Slf4j
public class QuestionCommentServiceImpl extends ServiceImpl<QuestionCommentMapper, QuestionComment>
        implements QuestionCommentService {

    /** 内容最大长度 */
    private static final int MAX_CONTENT_LENGTH = 2000;

    /** 自动隐藏的举报次数阈值 */
    private static final int AUTO_HIDE_REPORT_NUM = 3;

    @Resource
    private QuestionCommentLikeMapper commentLikeMapper;

    @Resource
    private QuestionCommentReportMapper commentReportMapper;

    @Resource
    private UserService userService;

    // ----------------------------------------------------------------
    //  1. 发表评论
    // ----------------------------------------------------------------

    @Override
    public Long addComment(CommentAddRequest request, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Long questionId = request.getQuestionId();
        String content = request.getContent();
        ThrowUtils.throwIf(questionId == null || questionId <= 0, ErrorCode.PARAMS_ERROR, "题目 id 不合法");
        ThrowUtils.throwIf(StringUtils.isBlank(content), ErrorCode.PARAMS_ERROR, "内容不能为空");
        ThrowUtils.throwIf(content.length() > MAX_CONTENT_LENGTH, ErrorCode.PARAMS_ERROR,
                "内容不能超过 " + MAX_CONTENT_LENGTH + " 字");

        // 深度校验：若有父评论则计算层级
        Long parentId = request.getParentId();
        if (parentId != null) {
            QuestionComment parent = getById(parentId);
            ThrowUtils.throwIf(parent == null || parent.getIsDelete() == 1, ErrorCode.NOT_FOUND_ERROR, "父评论不存在");
            // 检查深度，父评论若已有 parentId 则已经是第 2 级，再回复就是第 3 级
            // 若父评论是第 3 级，则将 parentId 设为该评论的 parentId（"引用上一级"）
            if (parent.getParentId() != null) {
                QuestionComment grandParent = getById(parent.getParentId());
                if (grandParent != null && grandParent.getParentId() != null) {
                    // 已是第 3 层，将 parentId 提升为 grandParent
                    parentId = parent.getParentId();
                }
            }
        }

        QuestionComment comment = new QuestionComment();
        comment.setQuestionId(questionId);
        comment.setUserId(loginUser.getId());
        comment.setParentId(parentId);
        comment.setReplyToId(request.getReplyToId());
        comment.setContent(content);
        comment.setLikeNum(0);
        comment.setReportNum(0);
        comment.setIsPinned(0);
        comment.setIsOfficial(0);
        comment.setStatus(0);

        boolean saved = save(comment);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR);
        return comment.getId();
    }

    // ----------------------------------------------------------------
    //  2. 删除评论
    // ----------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId, User loginUser) {
        ThrowUtils.throwIf(commentId == null || commentId <= 0, ErrorCode.PARAMS_ERROR);
        QuestionComment comment = getById(commentId);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);

        // 权限：仅本人或管理员
        boolean isOwner = comment.getUserId().equals(loginUser.getId());
        boolean isAdmin = userService.isAdmin(loginUser);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除该评论");
        }

        // 软删本评论
        boolean result = removeById(commentId);

        // 级联软删所有子评论
        LambdaUpdateWrapper<QuestionComment> childWrapper = new LambdaUpdateWrapper<>();
        childWrapper.eq(QuestionComment::getParentId, commentId)
                    .set(QuestionComment::getIsDelete, 1);
        update(childWrapper);

        return result;
    }

    // ----------------------------------------------------------------
    //  3. 分页获取评论列表（含子评论树）
    // ----------------------------------------------------------------

    @Override
    public Page<CommentVO> listCommentVOByPage(CommentQueryRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null || request.getQuestionId() == null, ErrorCode.PARAMS_ERROR);

        long current = request.getCurrent();
        long pageSize = Math.min(request.getPageSize(), 50);
        String sortField = StringUtils.isNotBlank(request.getSortField()) ? request.getSortField() : "createTime";
        String sortOrder = StringUtils.isNotBlank(request.getSortOrder()) ? request.getSortOrder() : CommonConstant.SORT_ORDER_DESC;

        // 查顶级评论（parentId IS NULL，status=0 正常）
        LambdaQueryWrapper<QuestionComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionComment::getQuestionId, request.getQuestionId())
               .isNull(QuestionComment::getParentId)
               .eq(QuestionComment::getStatus, 0)
               // 置顶评论优先
               .orderByDesc(QuestionComment::getIsPinned);

        if ("likeNum".equals(sortField)) {
            if (CommonConstant.SORT_ORDER_ASC.equals(sortOrder)) {
                wrapper.orderByAsc(QuestionComment::getLikeNum);
            } else {
                wrapper.orderByDesc(QuestionComment::getLikeNum);
            }
        } else {
            if (CommonConstant.SORT_ORDER_ASC.equals(sortOrder)) {
                wrapper.orderByAsc(QuestionComment::getCreateTime);
            } else {
                wrapper.orderByDesc(QuestionComment::getCreateTime);
            }
        }

        Page<QuestionComment> commentPage = page(new Page<>(current, pageSize), wrapper);

        // 获取当前登录用户（可为 null）
        User loginUser = userService.getLoginUserPermitNull(httpRequest);

        // 转 VO
        List<CommentVO> voList = buildCommentVOTree(commentPage.getRecords(), loginUser);

        Page<CommentVO> voPage = new Page<>(current, pageSize, commentPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    // ----------------------------------------------------------------
    //  4. 点赞 / 取消点赞
    // ----------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> likeComment(Long commentId, User loginUser) {
        ThrowUtils.throwIf(commentId == null || commentId <= 0, ErrorCode.PARAMS_ERROR);
        QuestionComment comment = getById(commentId);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);

        Long userId = loginUser.getId();

        // 检查是否已点赞
        LambdaQueryWrapper<QuestionCommentLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(QuestionCommentLike::getCommentId, commentId)
                   .eq(QuestionCommentLike::getUserId, userId);
        QuestionCommentLike existingLike = commentLikeMapper.selectOne(likeWrapper);

        boolean liked;
        int delta;
        if (existingLike != null) {
            // 取消点赞
            commentLikeMapper.deleteById(existingLike.getId());
            liked = false;
            delta = -1;
        } else {
            // 新增点赞
            QuestionCommentLike like = new QuestionCommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            liked = true;
            delta = 1;
        }

        // 更新冗余点赞数（乐观锁方式，保证不小于 0）
        LambdaUpdateWrapper<QuestionComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(QuestionComment::getId, commentId)
                     .setSql("likeNum = GREATEST(0, likeNum + " + delta + ")");
        update(updateWrapper);

        int newLikeNum = Math.max(0, comment.getLikeNum() + delta);
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeNum", newLikeNum);
        return result;
    }

    // ----------------------------------------------------------------
    //  5. 举报
    // ----------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reportComment(CommentReportRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null || request.getCommentId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(request.getReason()), ErrorCode.PARAMS_ERROR, "举报原因不能为空");

        QuestionComment comment = getById(request.getCommentId());
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);

        // 防止重复举报（唯一索引也会兜底）
        LambdaQueryWrapper<QuestionCommentReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.eq(QuestionCommentReport::getCommentId, request.getCommentId())
                     .eq(QuestionCommentReport::getUserId, loginUser.getId());
        if (commentReportMapper.selectCount(reportWrapper) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已举报过该评论");
        }

        QuestionCommentReport report = new QuestionCommentReport();
        report.setCommentId(request.getCommentId());
        report.setUserId(loginUser.getId());
        report.setReason(request.getReason());
        report.setStatus(0);
        commentReportMapper.insert(report);

        // 更新举报数并检查阈值
        LambdaUpdateWrapper<QuestionComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(QuestionComment::getId, request.getCommentId())
                     .setSql("reportNum = reportNum + 1");

        // 若举报次数达到阈值，自动进入待审核
        int newReportNum = comment.getReportNum() + 1;
        if (newReportNum >= AUTO_HIDE_REPORT_NUM) {
            updateWrapper.set(QuestionComment::getStatus, 1);
        }
        update(updateWrapper);
        return true;
    }

    // ----------------------------------------------------------------
    //  6. 置顶
    // ----------------------------------------------------------------

    @Override
    public boolean pinComment(Long commentId, boolean pinned) {
        ThrowUtils.throwIf(commentId == null || commentId <= 0, ErrorCode.PARAMS_ERROR);
        QuestionComment comment = getById(commentId);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);
        comment.setIsPinned(pinned ? 1 : 0);
        return updateById(comment);
    }

    // ----------------------------------------------------------------
    //  7. 官方解答
    // ----------------------------------------------------------------

    @Override
    public boolean setOfficialAnswer(Long commentId, boolean official) {
        ThrowUtils.throwIf(commentId == null || commentId <= 0, ErrorCode.PARAMS_ERROR);
        QuestionComment comment = getById(commentId);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);
        comment.setIsOfficial(official ? 1 : 0);
        return updateById(comment);
    }

    // ----------------------------------------------------------------
    //  工具方法：组装 CommentVO 树
    // ----------------------------------------------------------------

    @Override
    public List<CommentVO> buildCommentVOTree(List<QuestionComment> topComments, User loginUser) {
        if (topComments == null || topComments.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集顶级评论 id
        List<Long> parentIds = topComments.stream().map(QuestionComment::getId).collect(Collectors.toList());

        // 批量查询子评论（第一层，最多前 50 条）
        LambdaQueryWrapper<QuestionComment> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.in(QuestionComment::getParentId, parentIds)
                    .eq(QuestionComment::getStatus, 0)
                    .orderByAsc(QuestionComment::getCreateTime)
                    .last("LIMIT 500");
        List<QuestionComment> childComments = list(childWrapper);

        // 以 parentId 分组
        Map<Long, List<QuestionComment>> childrenMap = childComments.stream()
                .collect(Collectors.groupingBy(QuestionComment::getParentId));

        // 收集所有用户 id
        Set<Long> userIds = new HashSet<>();
        topComments.forEach(c -> userIds.add(c.getUserId()));
        childComments.forEach(c -> userIds.add(c.getUserId()));

        List<User> users = userService.listByIds(userIds);
        Map<Long, UserVO> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(u, vo);
                    return vo;
                }));

        // 若已登录，批量查询该用户对这些评论的点赞情况
        Set<Long> likedIds = new HashSet<>();
        if (loginUser != null) {
            List<Long> allCommentIds = new ArrayList<>(parentIds);
            childComments.forEach(c -> allCommentIds.add(c.getId()));
            if (!allCommentIds.isEmpty()) {
                LambdaQueryWrapper<QuestionCommentLike> likeWrapper = new LambdaQueryWrapper<>();
                likeWrapper.eq(QuestionCommentLike::getUserId, loginUser.getId())
                           .in(QuestionCommentLike::getCommentId, allCommentIds);
                commentLikeMapper.selectList(likeWrapper).forEach(l -> likedIds.add(l.getCommentId()));
            }
        }

        // 转 VO
        return topComments.stream().map(c -> toVO(c, userMap, childrenMap, likedIds)).collect(Collectors.toList());
    }

    private CommentVO toVO(QuestionComment comment, Map<Long, UserVO> userMap,
                           Map<Long, List<QuestionComment>> childrenMap, Set<Long> likedIds) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setQuestionId(comment.getQuestionId());
        vo.setParentId(comment.getParentId());
        vo.setReplyToId(comment.getReplyToId());
        vo.setContent(comment.getContent());
        vo.setLikeNum(comment.getLikeNum());
        vo.setIsPinned(comment.getIsPinned());
        vo.setIsOfficial(comment.getIsOfficial());
        vo.setStatus(comment.getStatus());
        vo.setCreateTime(comment.getCreateTime());
        vo.setDeleted(false);
        vo.setUser(userMap.getOrDefault(comment.getUserId(), null));
        vo.setHasLiked(likedIds.contains(comment.getId()));

        // 组装子评论（不递归第三层，直接平铺）
        List<QuestionComment> children = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());
        List<CommentVO> replyVOs = children.stream()
                .map(child -> {
                    CommentVO childVO = new CommentVO();
                    childVO.setId(child.getId());
                    childVO.setQuestionId(child.getQuestionId());
                    childVO.setParentId(child.getParentId());
                    childVO.setReplyToId(child.getReplyToId());
                    childVO.setContent(child.getContent());
                    childVO.setLikeNum(child.getLikeNum());
                    childVO.setIsPinned(child.getIsPinned());
                    childVO.setIsOfficial(child.getIsOfficial());
                    childVO.setStatus(child.getStatus());
                    childVO.setCreateTime(child.getCreateTime());
                    childVO.setDeleted(false);
                    childVO.setUser(userMap.getOrDefault(child.getUserId(), null));
                    childVO.setHasLiked(likedIds.contains(child.getId()));
                    childVO.setReplies(Collections.emptyList());
                    return childVO;
                })
                .collect(Collectors.toList());
        vo.setReplies(replyVOs);
        return vo;
    }
}
