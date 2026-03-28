package com.xduo.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.constant.CommonConstant;
    import com.xduo.springbootinit.manager.AiManager;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.mapper.QuestionMapper;
import com.xduo.springbootinit.mapper.UserQuestionHistoryMapper;
import com.xduo.springbootinit.model.dto.question.QuestionQueryRequest;
import com.xduo.springbootinit.model.entity.Question;
import com.xduo.springbootinit.model.entity.QuestionBankQuestion;
import com.xduo.springbootinit.model.entity.QuestionFavour;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.entity.UserQuestionHistory;
import com.xduo.springbootinit.service.QuestionFavourService;
import com.xduo.springbootinit.model.vo.QuestionVO;
import com.xduo.springbootinit.model.vo.ResumeQuestionRecommendVO;
import com.xduo.springbootinit.model.vo.UserVO;
import com.xduo.springbootinit.service.QuestionBankQuestionService;
import com.xduo.springbootinit.service.QuestionService;
import com.xduo.springbootinit.service.UserService;
import com.xduo.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.xduo.springbootinit.model.dto.question.QuestionEsDTO;
import com.xduo.springbootinit.exception.BusinessException;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.ArrayList;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Resource
    @Lazy
    private QuestionFavourService questionFavourService;

    @Resource
    private UserQuestionHistoryMapper userQuestionHistoryMapper;

    @Resource
    private AiManager aiManager;

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        String title = question.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String answer = questionQueryRequest.getAnswer();
        String searchText = questionQueryRequest.getSearchText();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        List<String> tagList = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);
        // 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);
        // 是否已收藏
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            QueryWrapper<QuestionFavour> favourQueryWrapper = new QueryWrapper<>();
            favourQueryWrapper.eq("questionId", question.getId());
            favourQueryWrapper.eq("userId", loginUser.getId());
            questionVO.setHasFavour(questionFavourService.getOne(favourQueryWrapper) != null);
        }
        // 收藏数
        QueryWrapper<QuestionFavour> favourCountWrapper = new QueryWrapper<>();
        favourCountWrapper.eq("questionId", question.getId());
        questionVO.setFavourNum((int) questionFavourService.count(favourCountWrapper));
        return questionVO;
    }

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(),
                questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            return QuestionVO.objToVo(question);
        }).collect(Collectors.toList());
        // 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        User loginUser = userService.getLoginUserPermitNull(request);
        // 查询收藏状态和收藏数
        Map<Long, Boolean> questionIdHasFavourMap = new java.util.HashMap<>();
        Map<Long, Integer> questionIdFavourNumMap = new java.util.HashMap<>();
        if (loginUser != null) {
            Set<Long> questionIdSet = questionList.stream().map(Question::getId).collect(Collectors.toSet());
            QueryWrapper<QuestionFavour> favourQueryWrapper = new QueryWrapper<>();
            favourQueryWrapper.in("questionId", questionIdSet);
            favourQueryWrapper.eq("userId", loginUser.getId());
            List<QuestionFavour> favourList = questionFavourService.list(favourQueryWrapper);
            Set<Long> favourQuestionIdSet = favourList.stream().map(QuestionFavour::getQuestionId).collect(Collectors.toSet());
            favourQuestionIdSet.forEach(id -> questionIdHasFavourMap.put(id, true));
        }
        // 批量查询收藏数 (推荐在实际项目中增加 favourNum 到 Question 表或使用 Redis 缓存计数)
        // 这里为了简单先用循环查或分组查
        Set<Long> allQuestionIdSet = questionList.stream().map(Question::getId).collect(Collectors.toSet());
        // mybatis-plus 暂时没有直接分组计数的快捷 IService 方法，这里简单处理
        allQuestionIdSet.forEach(qId -> {
            QueryWrapper<QuestionFavour> qw = new QueryWrapper<>();
            qw.eq("questionId", qId);
            questionIdFavourNumMap.put(qId, (int) questionFavourService.count(qw));
        });

        questionVOList.forEach(questionVO -> {
            Long userId = questionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUser(userService.getUserVO(user));
            questionVO.setHasFavour(questionIdHasFavourMap.getOrDefault(questionVO.getId(), false));
            questionVO.setFavourNum(questionIdFavourNumMap.getOrDefault(questionVO.getId(), 0));
        });
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

    @Override
    public Page<Question> searchFromEs(QuestionQueryRequest questionQueryRequest) {
        // 获取参数
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String searchText = questionQueryRequest.getSearchText();
        List<String> tags = questionQueryRequest.getTags();
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        Long userId = questionQueryRequest.getUserId();
        // 注意，ES 的起始页为 0
        int current = Math.max(0, questionQueryRequest.getCurrent() - 1);
        int pageSize = questionQueryRequest.getPageSize();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        // 过滤
        boolQueryBuilder.filter(f -> f.term(t -> t.field("isDelete").value(0)));
        if (id != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("id").value(id)));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(m -> m.term(t -> t.field("id").value(notId)));
        }
        if (userId != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("userId").value(userId)));
        }
        if (questionBankId != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("questionBankId").value(questionBankId)));
        }
        // 必须包含所有标签
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                boolQueryBuilder.filter(f -> f.term(t -> t.field("tags").value(tag)));
            }
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(s -> s.match(m -> m.field("title").query(searchText)));
            boolQueryBuilder.should(s -> s.match(m -> m.field("content").query(searchText)));
            boolQueryBuilder.should(s -> s.match(m -> m.field("answer").query(searchText)));
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
                .withPageable(org.springframework.data.domain.PageRequest.of(current, pageSize))
                .build();

        Page<Question> page = new Page<>();
        List<Question> resourceList = new ArrayList<>();
        try {
            SearchHits<QuestionEsDTO> searchHits = elasticsearchOperations.search(nativeQuery, QuestionEsDTO.class);
            page.setTotal(searchHits.getTotalHits());

            if (searchHits.hasSearchHits()) {
                List<SearchHit<QuestionEsDTO>> searchHitList = searchHits.getSearchHits();
                for (SearchHit<QuestionEsDTO> hit : searchHitList) {
                    resourceList.add(QuestionEsDTO.dtoToObj(hit.getContent()));
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
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteQuestions(List<Long> questionIdList) {
        if (CollUtil.isEmpty(questionIdList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "要删除的题目列表为空");
        }
        for (Long questionId : questionIdList) {
            boolean result = this.removeById(questionId);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除题目失败");
            }
            // 移除题目题库关系
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionId, questionId);
            result = questionBankQuestionService.remove(lambdaQueryWrapper);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除题目题库关联失败");
            }
        }
    }
    @Override
    public Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 题目表的查询条件
        QueryWrapper<Question> queryWrapper = this.getQueryWrapper(questionQueryRequest);
        // 根据题库查询题目列表接口
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        if (questionBankId != null) {
            // 查询题库内的题目 id
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .select(QuestionBankQuestion::getQuestionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            List<QuestionBankQuestion> questionList = questionBankQuestionService.list(lambdaQueryWrapper);
            if (CollUtil.isEmpty(questionList)) {
                Page<Question> emptyPage = new Page<>(current, size, 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            // 取出题目 id 集合
            Set<Long> questionIdSet = questionList.stream()
                    .map(QuestionBankQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            // 复用原有题目表的查询条件
            queryWrapper.in("id", questionIdSet);
        }
        // 查询数据库
        Page<Question> questionPage = this.page(new Page<>(current, size), queryWrapper);
        return questionPage;
    }

    @Override
    public List<QuestionVO> listRecommendQuestionVOByUser(long userId, Long currentQuestionId, int size, HttpServletRequest request) {
        size = Math.max(1, Math.min(size, 12));
        Map<String, Integer> tagWeightMap = new HashMap<>();
        Set<Long> interactedQuestionIdSet = new HashSet<>();

        QueryWrapper<UserQuestionHistory> historyQueryWrapper = new QueryWrapper<>();
        historyQueryWrapper.eq("userId", userId);
        List<UserQuestionHistory> historyList = userQuestionHistoryMapper.selectList(historyQueryWrapper);
        if (CollUtil.isNotEmpty(historyList)) {
            Set<Long> historyQuestionIdSet = historyList.stream()
                    .map(UserQuestionHistory::getQuestionId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            interactedQuestionIdSet.addAll(historyQuestionIdSet);
            Map<Long, Integer> historyStatusMap = historyList.stream()
                    .filter(item -> item.getQuestionId() != null)
                    .collect(Collectors.toMap(UserQuestionHistory::getQuestionId, UserQuestionHistory::getStatus, (a, b) -> Math.max(a, b)));
            this.listByIds(historyQuestionIdSet).forEach(question -> {
                int weight = convertHistoryStatusToWeight(historyStatusMap.getOrDefault(question.getId(), 0));
                addTagWeights(tagWeightMap, parseTagList(question.getTags()), weight);
            });
        }

        QueryWrapper<QuestionFavour> favourQueryWrapper = new QueryWrapper<>();
        favourQueryWrapper.eq("userId", userId);
        List<QuestionFavour> favourList = questionFavourService.list(favourQueryWrapper);
        if (CollUtil.isNotEmpty(favourList)) {
            Set<Long> favourQuestionIdSet = favourList.stream()
                    .map(QuestionFavour::getQuestionId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            interactedQuestionIdSet.addAll(favourQuestionIdSet);
            this.listByIds(favourQuestionIdSet).forEach(question -> addTagWeights(tagWeightMap, parseTagList(question.getTags()), 5));
        }

        if (currentQuestionId != null && currentQuestionId > 0) {
            Question currentQuestion = this.getById(currentQuestionId);
            if (currentQuestion != null) {
                addTagWeights(tagWeightMap, parseTagList(currentQuestion.getTags()), 2);
            }
        }

        List<Question> candidateQuestionList = listRecommendationCandidateQuestions(300, currentQuestionId);
        if (CollUtil.isEmpty(candidateQuestionList)) {
            return Collections.emptyList();
        }

        List<QuestionRecommendationScore> scoredList = candidateQuestionList.stream()
                .filter(question -> currentQuestionId == null || !question.getId().equals(currentQuestionId))
                .filter(question -> !interactedQuestionIdSet.contains(question.getId()))
                .map(question -> buildRecommendationScore(question, tagWeightMap, "偏好标签"))
                .filter(item -> item.getScore() > 0)
                .sorted(Comparator
                        .comparingInt(QuestionRecommendationScore::getScore).reversed()
                        .thenComparing(QuestionRecommendationScore::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(size)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(scoredList)) {
            List<Question> fallbackList = candidateQuestionList.stream()
                    .filter(question -> currentQuestionId == null || !question.getId().equals(currentQuestionId))
                    .sorted(Comparator.comparing(Question::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(size)
                    .collect(Collectors.toList());
            return fallbackList.stream().map(question -> {
                QuestionVO questionVO = this.getQuestionVO(question, request);
                questionVO.setRecommendReason("根据最新题目为你补充推荐");
                return questionVO;
            }).collect(Collectors.toList());
        }

        return scoredList.stream().map(item -> {
            QuestionVO questionVO = this.getQuestionVO(item.getQuestion(), request);
            questionVO.setRecommendReason(item.getReason());
            return questionVO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<QuestionVO> listRelatedQuestionVO(long questionId, int size, HttpServletRequest request) {
        size = Math.max(1, Math.min(size, 12));
        Question currentQuestion = this.getById(questionId);
        if (currentQuestion == null) {
            return Collections.emptyList();
        }
        List<String> currentTagList = parseTagList(currentQuestion.getTags());
        List<Question> candidateQuestionList = listRecommendationCandidateQuestions(300, questionId);
        if (CollUtil.isEmpty(candidateQuestionList)) {
            return Collections.emptyList();
        }

        List<QuestionRecommendationScore> scoredList = candidateQuestionList.stream()
                .filter(question -> !question.getId().equals(questionId))
                .map(question -> {
                    List<String> candidateTags = parseTagList(question.getTags());
                    List<String> overlapTags = candidateTags.stream()
                            .filter(currentTagList::contains)
                            .distinct()
                            .collect(Collectors.toList());
                    int score = overlapTags.size() * 100;
                    String reason = overlapTags.isEmpty() ? "" : "关联标签：" + String.join(" / ", overlapTags);
                    return new QuestionRecommendationScore(question, score, reason);
                })
                .filter(item -> item.getScore() > 0)
                .sorted(Comparator
                        .comparingInt(QuestionRecommendationScore::getScore).reversed()
                        .thenComparing(QuestionRecommendationScore::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(size)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(scoredList)) {
            return candidateQuestionList.stream()
                    .filter(question -> !question.getId().equals(questionId))
                    .sorted(Comparator.comparing(Question::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(size)
                    .map(question -> {
                        QuestionVO questionVO = this.getQuestionVO(question, request);
                        questionVO.setRecommendReason("为你补充同主题下的最新题目");
                        return questionVO;
                    })
                    .collect(Collectors.toList());
        }

        return scoredList.stream().map(item -> {
            QuestionVO questionVO = this.getQuestionVO(item.getQuestion(), request);
            questionVO.setRecommendReason(item.getReason());
            return questionVO;
        }).collect(Collectors.toList());
    }

    @Override
    public ResumeQuestionRecommendVO recommendQuestionsByResume(long userId, String resumeText, int size, HttpServletRequest request) {
        ThrowUtils.throwIf(StringUtils.isBlank(resumeText), ErrorCode.PARAMS_ERROR, "请先粘贴简历内容");
        String trimmedResumeText = resumeText.trim();
        ThrowUtils.throwIf(trimmedResumeText.length() < 20, ErrorCode.PARAMS_ERROR, "简历内容过短，请补充更多项目经历或技能描述");
        size = Math.max(1, Math.min(size, 12));

        List<Question> allQuestionList = listRecommendationCandidateQuestions(500, null);
        ResumeQuestionRecommendVO result = new ResumeQuestionRecommendVO();
        if (CollUtil.isEmpty(allQuestionList)) {
            result.setJobDirection("待识别");
            result.setExtractedTags(Collections.emptyList());
            result.setAnalysisSummary("当前题库为空，暂时无法给出题目推荐。");
            result.setRecommendFocus("请先补充题目数据");
            result.setAnalysisSource("系统规则分析");
            result.setQuestionList(Collections.emptyList());
            return result;
        }

        Set<String> candidateTagSet = allQuestionList.stream()
                .flatMap(question -> parseTagList(question.getTags()).stream())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        ResumeAnalysisProfile profile = buildResumeAnalysisProfile(trimmedResumeText, candidateTagSet);

        Map<String, Integer> tagWeightMap = new HashMap<>();
        addTagWeights(tagWeightMap, profile.getExtractedTags(), 6);
        addBehaviorPreferenceWeights(userId, tagWeightMap);
        Set<Long> interactedQuestionIdSet = getInteractedQuestionIdSet(userId);

        List<QuestionRecommendationScore> scoredList = allQuestionList.stream()
                .filter(question -> !interactedQuestionIdSet.contains(question.getId()))
                .map(question -> buildResumeRecommendationScore(question, tagWeightMap, profile.getExtractedTags()))
                .filter(item -> item.getScore() > 0)
                .sorted(Comparator
                        .comparingInt(QuestionRecommendationScore::getScore).reversed()
                        .thenComparing(QuestionRecommendationScore::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(size)
                .collect(Collectors.toList());

        List<QuestionVO> questionVOList;
        if (CollUtil.isEmpty(scoredList)) {
            questionVOList = allQuestionList.stream()
                    .sorted(Comparator.comparing(Question::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(size)
                    .map(question -> {
                        QuestionVO questionVO = this.getQuestionVO(question, request);
                        questionVO.setRecommendReason("根据简历关键词为你补充推荐最新题目");
                        return questionVO;
                    })
                    .collect(Collectors.toList());
        } else {
            questionVOList = scoredList.stream().map(item -> {
                QuestionVO questionVO = this.getQuestionVO(item.getQuestion(), request);
                questionVO.setRecommendReason(item.getReason());
                return questionVO;
            }).collect(Collectors.toList());
        }

        result.setJobDirection(profile.getJobDirection());
        result.setExtractedTags(profile.getExtractedTags());
        result.setAnalysisSummary(profile.getAnalysisSummary());
        result.setRecommendFocus(profile.getRecommendFocus());
        result.setAnalysisSource(profile.getAnalysisSource());
        result.setQuestionList(questionVOList);
        return result;
    }

    private int convertHistoryStatusToWeight(Integer status) {
        if (status == null) {
            return 1;
        }
        switch (status) {
            case 1:
                return 4;
            case 2:
                return 3;
            default:
                return 1;
        }
    }

    private void addTagWeights(Map<String, Integer> tagWeightMap, List<String> tagList, int weight) {
        if (CollUtil.isEmpty(tagList) || weight <= 0) {
            return;
        }
        for (String tag : tagList) {
            if (StringUtils.isBlank(tag)) {
                continue;
            }
            tagWeightMap.merge(tag.trim(), weight, Integer::sum);
        }
    }

    private List<String> parseTagList(String tags) {
        if (StringUtils.isBlank(tags)) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(tags, String.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 推荐场景只取一批最新题目做候选，避免每次把整张题目表拉到应用层。
     */
    private List<Question> listRecommendationCandidateQuestions(int limit, Long excludeQuestionId) {
        int candidateLimit = Math.max(20, Math.min(limit, 500));
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("updateTime");
        queryWrapper.last("limit " + (excludeQuestionId == null ? candidateLimit : candidateLimit + 1));
        List<Question> candidateList = this.list(queryWrapper);
        if (excludeQuestionId == null) {
            return candidateList;
        }
        return candidateList.stream()
                .filter(question -> !excludeQuestionId.equals(question.getId()))
                .limit(candidateLimit)
                .collect(Collectors.toList());
    }

    private QuestionRecommendationScore buildRecommendationScore(Question question, Map<String, Integer> tagWeightMap, String reasonPrefix) {
        List<String> matchedTagList = new ArrayList<>();
        int score = 0;
        for (String tag : parseTagList(question.getTags())) {
            Integer weight = tagWeightMap.get(tag);
            if (weight != null && weight > 0) {
                score += weight;
                matchedTagList.add(tag);
            }
        }
        String reason = matchedTagList.isEmpty() ? "" : reasonPrefix + "：" + String.join(" / ", matchedTagList.stream().distinct().limit(3).collect(Collectors.toList()));
        return new QuestionRecommendationScore(question, score, reason);
    }

    private QuestionRecommendationScore buildResumeRecommendationScore(Question question, Map<String, Integer> tagWeightMap, List<String> extractedTags) {
        List<String> matchedTagList = new ArrayList<>();
        int score = 0;
        for (String tag : parseTagList(question.getTags())) {
            Integer weight = tagWeightMap.get(tag);
            if (weight != null && weight > 0) {
                score += weight;
                matchedTagList.add(tag);
            }
        }
        List<String> matchedKeywordList = new ArrayList<>();
        String normalizedQuestionText = normalizeKeyword(question.getTitle() + " " + question.getContent() + " " + question.getAnswer());
        for (String tag : extractedTags) {
            if (StringUtils.isBlank(tag) || matchedTagList.contains(tag)) {
                continue;
            }
            if (normalizedQuestionText.contains(normalizeKeyword(tag))) {
                score += 2;
                matchedKeywordList.add(tag);
            }
        }
        String reason = buildResumeRecommendationReason(matchedTagList, matchedKeywordList);
        return new QuestionRecommendationScore(question, score, reason);
    }

    private String buildResumeRecommendationReason(List<String> matchedTagList, List<String> matchedKeywordList) {
        List<String> reasonList = new ArrayList<>();
        if (CollUtil.isNotEmpty(matchedTagList)) {
            reasonList.add("简历技能匹配：" + String.join(" / ", matchedTagList.stream().distinct().limit(3).collect(Collectors.toList())));
        }
        if (CollUtil.isNotEmpty(matchedKeywordList)) {
            reasonList.add("内容关键词命中：" + String.join(" / ", matchedKeywordList.stream().distinct().limit(2).collect(Collectors.toList())));
        }
        return String.join("；", reasonList);
    }

    private void addBehaviorPreferenceWeights(long userId, Map<String, Integer> tagWeightMap) {
        QueryWrapper<UserQuestionHistory> historyQueryWrapper = new QueryWrapper<>();
        historyQueryWrapper.eq("userId", userId);
        List<UserQuestionHistory> historyList = userQuestionHistoryMapper.selectList(historyQueryWrapper);
        if (CollUtil.isNotEmpty(historyList)) {
            Set<Long> historyQuestionIdSet = historyList.stream()
                    .map(UserQuestionHistory::getQuestionId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, Integer> historyStatusMap = historyList.stream()
                    .filter(item -> item.getQuestionId() != null)
                    .collect(Collectors.toMap(UserQuestionHistory::getQuestionId, UserQuestionHistory::getStatus, (a, b) -> Math.max(a, b)));
            this.listByIds(historyQuestionIdSet).forEach(question -> {
                int weight = Math.max(1, convertHistoryStatusToWeight(historyStatusMap.getOrDefault(question.getId(), 0)) - 1);
                addTagWeights(tagWeightMap, parseTagList(question.getTags()), weight);
            });
        }

        QueryWrapper<QuestionFavour> favourQueryWrapper = new QueryWrapper<>();
        favourQueryWrapper.eq("userId", userId);
        List<QuestionFavour> favourList = questionFavourService.list(favourQueryWrapper);
        if (CollUtil.isNotEmpty(favourList)) {
            Set<Long> favourQuestionIdSet = favourList.stream()
                    .map(QuestionFavour::getQuestionId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            this.listByIds(favourQuestionIdSet).forEach(question -> addTagWeights(tagWeightMap, parseTagList(question.getTags()), 2));
        }
    }

    private Set<Long> getInteractedQuestionIdSet(long userId) {
        Set<Long> interactedQuestionIdSet = new HashSet<>();
        QueryWrapper<UserQuestionHistory> historyQueryWrapper = new QueryWrapper<>();
        historyQueryWrapper.eq("userId", userId);
        List<UserQuestionHistory> historyList = userQuestionHistoryMapper.selectList(historyQueryWrapper);
        if (CollUtil.isNotEmpty(historyList)) {
            interactedQuestionIdSet.addAll(historyList.stream()
                    .map(UserQuestionHistory::getQuestionId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet()));
        }
        QueryWrapper<QuestionFavour> favourQueryWrapper = new QueryWrapper<>();
        favourQueryWrapper.eq("userId", userId);
        List<QuestionFavour> favourList = questionFavourService.list(favourQueryWrapper);
        if (CollUtil.isNotEmpty(favourList)) {
            interactedQuestionIdSet.addAll(favourList.stream()
                    .map(QuestionFavour::getQuestionId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet()));
        }
        return interactedQuestionIdSet;
    }

    private ResumeAnalysisProfile buildResumeAnalysisProfile(String resumeText, Set<String> candidateTagSet) {
        ResumeAnalysisProfile fallbackProfile = buildResumeAnalysisProfileByRule(resumeText, candidateTagSet);
        try {
            String systemPrompt = "你是一名技术面试教练，请从候选人的简历中提取岗位方向、核心技能标签和建议补强方向。"
                    + "请严格返回 JSON，不要输出额外解释。JSON 结构为："
                    + "{\"jobDirection\":\"\","
                    + "\"analysisSummary\":\"\","
                    + "\"suggestedTags\":[\"\"],"
                    + "\"recommendFocus\":\"\"}";
            String tagPoolText = candidateTagSet.stream().limit(80).collect(Collectors.joining("、"));
            String userPrompt = "候选标签池：" + tagPoolText + "\n"
                    + "请优先从标签池中选择最相关的技能标签，如果标签池不足也可以补充少量简洁标签。\n"
                    + "简历内容如下：\n" + resumeText;
            String aiResult = aiManager.doChat(systemPrompt, userPrompt);
            ResumeAnalysisProfile aiProfile = parseResumeAnalysisProfile(aiResult);
            if (CollUtil.isEmpty(aiProfile.getExtractedTags())) {
                aiProfile.setExtractedTags(fallbackProfile.getExtractedTags());
            }
            if (StringUtils.isBlank(aiProfile.getJobDirection())) {
                aiProfile.setJobDirection(fallbackProfile.getJobDirection());
            }
            if (StringUtils.isBlank(aiProfile.getAnalysisSummary())) {
                aiProfile.setAnalysisSummary(fallbackProfile.getAnalysisSummary());
            }
            if (StringUtils.isBlank(aiProfile.getRecommendFocus())) {
                aiProfile.setRecommendFocus(fallbackProfile.getRecommendFocus());
            }
            if (StringUtils.isBlank(aiProfile.getAnalysisSource())) {
                aiProfile.setAnalysisSource("AI 智能解析");
            }
            return aiProfile;
        } catch (Exception e) {
            log.info("简历 AI 解析失败，改用规则分析：{}", e.getMessage());
            return fallbackProfile;
        }
    }

    private ResumeAnalysisProfile parseResumeAnalysisProfile(String aiResult) {
        String jsonText = extractJsonText(aiResult);
        JSONObject jsonObject = JSONUtil.parseObj(jsonText);
        ResumeAnalysisProfile profile = new ResumeAnalysisProfile();
        profile.setJobDirection(jsonObject.getStr("jobDirection"));
        profile.setAnalysisSummary(jsonObject.getStr("analysisSummary"));
        profile.setRecommendFocus(jsonObject.getStr("recommendFocus"));
        profile.setAnalysisSource("AI 智能解析");
        profile.setExtractedTags(parseJsonStringList(jsonObject.get("suggestedTags")));
        return profile;
    }

    private ResumeAnalysisProfile buildResumeAnalysisProfileByRule(String resumeText, Set<String> candidateTagSet) {
        String normalizedResumeText = normalizeKeyword(resumeText);
        LinkedHashSet<String> matchedTagSet = new LinkedHashSet<>();
        for (String tag : candidateTagSet) {
            if (StringUtils.isBlank(tag)) {
                continue;
            }
            String normalizedTag = normalizeKeyword(tag);
            if (StringUtils.isNotBlank(normalizedTag) && normalizedResumeText.contains(normalizedTag)) {
                matchedTagSet.add(tag.trim());
            }
        }

        Map<String, List<String>> aliasMap = buildTagAliasMap();
        for (Map.Entry<String, List<String>> entry : aliasMap.entrySet()) {
            for (String alias : entry.getValue()) {
                if (normalizedResumeText.contains(normalizeKeyword(alias))) {
                    matchedTagSet.add(entry.getKey());
                    break;
                }
            }
        }

        List<String> extractedTags = matchedTagSet.stream().limit(8).collect(Collectors.toList());
        String jobDirection = inferJobDirection(extractedTags);
        String recommendFocus = extractedTags.isEmpty()
                ? "建议先补充目标岗位、项目经历和核心技术栈描述，系统才能给出更精准的推荐。"
                : "建议围绕 " + String.join(" / ", extractedTags.stream().limit(4).collect(Collectors.toList())) + " 做专项强化。";
        String analysisSummary = extractedTags.isEmpty()
                ? "系统暂未从简历中提取到明确技能关键词，先按通用高频面试题为你兜底推荐。"
                : "系统识别你更偏向 " + jobDirection + "，并从简历中提取出 " + String.join(" / ", extractedTags.stream().limit(5).collect(Collectors.toList())) + " 等技能关键词。";

        ResumeAnalysisProfile profile = new ResumeAnalysisProfile();
        profile.setJobDirection(jobDirection);
        profile.setExtractedTags(extractedTags);
        profile.setAnalysisSummary(analysisSummary);
        profile.setRecommendFocus(recommendFocus);
        profile.setAnalysisSource("系统规则分析");
        return profile;
    }

    private Map<String, List<String>> buildTagAliasMap() {
        Map<String, List<String>> aliasMap = new LinkedHashMap<>();
        aliasMap.put("Java", List.of("java", "jvm", "spring", "spring boot", "mybatis"));
        aliasMap.put("Spring Boot", List.of("spring boot", "springboot", "spring cloud"));
        aliasMap.put("MySQL", List.of("mysql", "sql", "innodb"));
        aliasMap.put("Redis", List.of("redis", "缓存", "cache"));
        aliasMap.put("消息队列", List.of("kafka", "rocketmq", "rabbitmq", "消息队列"));
        aliasMap.put("微服务", List.of("微服务", "spring cloud", "nacos", "gateway"));
        aliasMap.put("Linux", List.of("linux", "shell", "centos", "ubuntu"));
        aliasMap.put("Docker", List.of("docker", "容器"));
        aliasMap.put("前端", List.of("react", "vue", "next.js", "javascript", "typescript", "css", "html"));
        aliasMap.put("React", List.of("react", "react hooks", "next.js"));
        aliasMap.put("Vue", List.of("vue", "vue3", "nuxt"));
        aliasMap.put("JavaScript", List.of("javascript", "js", "es6"));
        aliasMap.put("TypeScript", List.of("typescript", "ts"));
        aliasMap.put("Python", List.of("python", "django", "flask", "fastapi"));
        aliasMap.put("算法", List.of("算法", "数据结构", "leetcode", "复杂度"));
        aliasMap.put("计算机网络", List.of("tcp", "udp", "http", "https", "网络"));
        aliasMap.put("操作系统", List.of("操作系统", "进程", "线程", "内存管理"));
        return aliasMap;
    }

    private String inferJobDirection(List<String> extractedTags) {
        List<String> lowerTagList = extractedTags.stream()
                .map(tag -> tag == null ? "" : tag.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        if (lowerTagList.stream().anyMatch(tag -> tag.contains("react") || tag.contains("vue") || tag.contains("javascript") || tag.contains("typescript") || tag.contains("前端"))) {
            return "前端开发";
        }
        if (lowerTagList.stream().anyMatch(tag -> tag.contains("python") || tag.contains("算法"))) {
            return "算法 / AI 岗位";
        }
        if (lowerTagList.stream().anyMatch(tag -> tag.contains("java") || tag.contains("spring") || tag.contains("redis") || tag.contains("mysql") || tag.contains("微服务"))) {
            return "Java 后端开发";
        }
        if (lowerTagList.stream().anyMatch(tag -> tag.contains("测试") || tag.contains("qa"))) {
            return "测试开发";
        }
        return "综合技术岗位";
    }

    private String normalizeKeyword(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("[\\s_\\-./()（）:：,，;；]+", "");
    }

    private String extractJsonText(String text) {
        if (StringUtils.isBlank(text)) {
            return "{}";
        }
        String cleanedText = text.trim();
        if (cleanedText.startsWith("```")) {
            cleanedText = cleanedText.replaceFirst("^```[a-zA-Z]*\\s*", "").replaceFirst("\\s*```$", "");
        }
        int start = cleanedText.indexOf('{');
        int end = cleanedText.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleanedText.substring(start, end + 1);
        }
        return cleanedText;
    }

    private List<String> parseJsonStringList(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof JSONArray jsonArray) {
            return jsonArray.stream()
                    .map(String::valueOf)
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
        }
        String text = String.valueOf(value);
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        return java.util.Arrays.stream(text.split("[,，/、\\s]+"))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private static class QuestionRecommendationScore {
        private final Question question;
        private final int score;
        private final String reason;

        private QuestionRecommendationScore(Question question, int score, String reason) {
            this.question = question;
            this.score = score;
            this.reason = reason;
        }

        public Question getQuestion() {
            return question;
        }

        public int getScore() {
            return score;
        }

        public String getReason() {
            return reason;
        }

        public java.util.Date getUpdateTime() {
            return question.getUpdateTime();
        }
    }

    private static class ResumeAnalysisProfile {
        private String jobDirection;
        private List<String> extractedTags = Collections.emptyList();
        private String analysisSummary;
        private String recommendFocus;
        private String analysisSource;

        public String getJobDirection() {
            return jobDirection;
        }

        public void setJobDirection(String jobDirection) {
            this.jobDirection = jobDirection;
        }

        public List<String> getExtractedTags() {
            return extractedTags;
        }

        public void setExtractedTags(List<String> extractedTags) {
            this.extractedTags = extractedTags == null ? Collections.emptyList() : extractedTags.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
        }

        public String getAnalysisSummary() {
            return analysisSummary;
        }

        public void setAnalysisSummary(String analysisSummary) {
            this.analysisSummary = analysisSummary;
        }

        public String getRecommendFocus() {
            return recommendFocus;
        }

        public void setRecommendFocus(String recommendFocus) {
            this.recommendFocus = recommendFocus;
        }

        public String getAnalysisSource() {
            return analysisSource;
        }

        public void setAnalysisSource(String analysisSource) {
            this.analysisSource = analysisSource;
        }
    }



}
