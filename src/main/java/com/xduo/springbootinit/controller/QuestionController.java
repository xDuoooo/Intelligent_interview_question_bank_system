package com.xduo.springbootinit.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.DeleteRequest;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.mapper.CounterManager;
import com.xduo.springbootinit.model.dto.question.*;
import com.xduo.springbootinit.model.entity.Question;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.vo.QuestionVO;
import com.xduo.springbootinit.model.vo.ResumeQuestionRecommendVO;
import com.xduo.springbootinit.service.QuestionSearchLogService;
import com.xduo.springbootinit.service.QuestionService;
import com.xduo.springbootinit.service.SecurityAlertService;
import com.xduo.springbootinit.service.UserService;
import com.xduo.springbootinit.utils.NetUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import com.xduo.springbootinit.service.UserQuestionHistoryService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.xduo.springbootinit.manager.AiManager;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private AiManager aiManager;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserQuestionHistoryService userQuestionHistoryService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionSearchLogService questionSearchLogService;

    @Resource
    private SecurityAlertService securityAlertService;

    @Resource
    private CounterManager counterManager;

    /**
     * 创建题目
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest,
                                          HttpServletRequest request) {
        //参数校验(非空)
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
        //构建实例
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        //封装Tag
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        // 数据校验
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除题目
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        // 数据校验
        questionService.validQuestion(question, false);
        // 判断是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //爬虫检测
        User loginUser = userService.getLoginUser(request);
        crawlerDetect(loginUser, request);
        // 查询数据库
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        // 保存浏览记录
        userQuestionHistoryService.addQuestionHistory(loginUser.getId(), id, 0);
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 获取个性化推荐题目
     */
    @GetMapping("/recommend/personal")
    public BaseResponse<List<QuestionVO>> listPersonalRecommendQuestionVO(Long questionId,
                                                                          @RequestParam(defaultValue = "6") Integer size,
                                                                          HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(questionService.listRecommendQuestionVOByUser(loginUser.getId(), questionId, size, request));
    }

    /**
     * 获取相关题目推荐
     */
    @GetMapping("/recommend/related")
    public BaseResponse<List<QuestionVO>> listRelatedQuestionVO(@RequestParam Long questionId,
                                                                @RequestParam(defaultValue = "6") Integer size,
                                                                HttpServletRequest request) {
        ThrowUtils.throwIf(questionId == null || questionId <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(questionService.listRelatedQuestionVO(questionId, size, request));
    }

    /**
     * 基于简历内容推荐题目
     */
    @PostMapping("/recommend/resume")
    public BaseResponse<ResumeQuestionRecommendVO> recommendQuestionsByResume(@RequestBody QuestionResumeRecommendRequest resumeRecommendRequest,
                                                                              HttpServletRequest request) {
        ThrowUtils.throwIf(resumeRecommendRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int size = resumeRecommendRequest.getSize() == null ? 6 : resumeRecommendRequest.getSize();
        ResumeQuestionRecommendVO result = questionService.recommendQuestionsByResume(
                loginUser.getId(),
                resumeRecommendRequest.getResumeText(),
                size,
                request
        );
        return ResultUtils.success(result);
    }

    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Entry entry = null;
        String remoteAddr = request.getRemoteAddr();
        try {
            entry = SphU.entry("listQuestionVOByPage", EntryType.IN, 1, remoteAddr);
            // 被保护的业务逻辑

            // 限制爬虫
            ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
            // 查询数据库
            Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
            // 获取封装类
            return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
        } catch (Throwable ex) {
            // 业务异常
            if (!BlockException.isBlockException(ex)) {
                Tracer.trace(ex);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
            }
            // 降级操作
            if (ex instanceof DegradeException) {
                return handleFallback(questionQueryRequest, request, ex);
            }
            // 限流操作
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "访问过于频繁，请稍后再试");
        } finally {
            if (entry != null) {
                entry.exit(1, remoteAddr);
            }
        }
    }

    /**
     * 分页获取当前登录用户创建的题目列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 编辑题目（给用户使用）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest,
                                              HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        // 数据校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = questionEditRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/search/page/vo")
    public BaseResponse<Page<QuestionVO>> searchQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.searchFromEs(questionQueryRequest);
        if (StringUtils.isNotBlank(questionQueryRequest.getSearchText())) {
            try {
                User loginUser = userService.getLoginUserPermitNull(request);
                questionSearchLogService.recordSearch(
                        loginUser == null ? null : loginUser.getId(),
                        questionQueryRequest.getSearchText(),
                        questionPage.getTotal(),
                        "question",
                        request.getRemoteAddr()
                );
            } catch (Exception e) {
                log.warn("记录题目搜索日志失败，searchText={}", questionQueryRequest.getSearchText(), e);
            }
        }
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    @PostMapping("/delete/batch")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchDeleteQuestions(@RequestBody QuestionBatchDeleteRequest questionBatchDeleteRequest,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(questionBatchDeleteRequest == null, ErrorCode.PARAMS_ERROR);
        questionService.batchDeleteQuestions(questionBatchDeleteRequest.getQuestionIdList());
        return ResultUtils.success(true);
    }

    /**
     * AI 批量生成题目
     *
     * @param questionAiGenerateRequest
     * @return
     */
    @PostMapping("/ai/generate/question")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> batchGenerateQuestionsByAi(@RequestBody QuestionAIGenerateRequest questionAiGenerateRequest,
                                                            HttpServletRequest request) {
        ThrowUtils.throwIf(questionAiGenerateRequest == null, ErrorCode.PARAMS_ERROR);
        String questionType = questionAiGenerateRequest.getQuestionType();
        Integer number = questionAiGenerateRequest.getNumber();
        ThrowUtils.throwIf(StringUtils.isBlank(questionType), ErrorCode.PARAMS_ERROR, "题目方向不能为空");
        ThrowUtils.throwIf(number == null || number < 1 || number > 20, ErrorCode.PARAMS_ERROR, "生成数量需在 1 到 20 之间");

        // 获取登录用户
        User loginUser = userService.getLoginUser(request);

        // 构造 Prompt
        String systemPrompt = "你是一位资深技术面试官，负责为面试题库生产可直接入库的高质量题目。"
                + "请严格输出 JSON 数组，不要输出 Markdown 代码块，不要输出额外解释。"
                + "数组中的每个对象必须包含 title、content、tags、answer 四个字段。"
                + "其中 tags 必须是字符串数组，title 简洁明确，content 描述题目要求，answer 给出结构化参考答案。";
        String userPrompt = String.format("知识点：%s，数量：%d", questionType, number);

        // 调用 AI
        String result = aiManager.doChat(systemPrompt, userPrompt);

        try {
            List<AiGeneratedQuestion> generatedQuestionList = parseAiGeneratedQuestions(result);
            int successCount = 0;
            for (AiGeneratedQuestion generatedQuestion : generatedQuestionList) {
                if (StringUtils.isAnyBlank(generatedQuestion.getTitle(), generatedQuestion.getContent(), generatedQuestion.getAnswer())) {
                    continue;
                }
                Question question = new Question();
                question.setTitle(generatedQuestion.getTitle().trim());
                question.setContent(generatedQuestion.getContent().trim());
                question.setAnswer(generatedQuestion.getAnswer().trim());
                if (generatedQuestion.getTags() != null) {
                    question.setTags(JSONUtil.toJsonStr(generatedQuestion.getTags()));
                }
                question.setUserId(loginUser.getId());
                questionService.validQuestion(question, true);
                questionService.save(question);
                successCount++;
            }
            ThrowUtils.throwIf(successCount == 0, ErrorCode.SYSTEM_ERROR, "AI 未生成可保存的题目");
            return ResultUtils.success(successCount);
        } catch (Exception e) {
            log.error("AI 结果解析失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成数据格式不正确");
        }
    }

    /**
     * 检测爬虫
     *
     * @param loginUserId
     */
    private void crawlerDetect(User loginUser, HttpServletRequest request) {
        long loginUserId = loginUser.getId();
        // 调用多少次时告警
        final int WARN_COUNT = 10;
        // 超过多少次封号
        final int BAN_COUNT = 20;
        String ip = NetUtils.getIpAddress(request);
        // 拼接访问 key
        String key = String.format("user:access:%s", loginUserId);
        // 一分钟内访问次数，180 秒过期
        long count = counterManager.incrAndGetCounter(key, 1, TimeUnit.MINUTES, 180);
        // 是否封号
        if (count > BAN_COUNT) {
            securityAlertService.recordAlert(
                    loginUserId,
                    loginUser.getUserName(),
                    "HIGH_FREQUENCY_ACCESS_BAN",
                    "high",
                    "用户在 1 分钟内访问题目详情次数过多，已自动封禁",
                    String.format("当前访问次数：%d，阈值：%d", count, BAN_COUNT),
                    ip
            );
            // 踢下线
            StpUtil.kickout(loginUserId);
            // 封号
            User updateUser = new User();
            updateUser.setId(loginUserId);
            updateUser.setUserRole("ban");
            userService.updateById(updateUser);
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "访问太频繁，已被封号");
        }
        // 是否告警
        if (count == WARN_COUNT) {
            securityAlertService.recordAlert(
                    loginUserId,
                    loginUser.getUserName(),
                    "HIGH_FREQUENCY_ACCESS_WARN",
                    "medium",
                    "用户在 1 分钟内高频访问题目详情，已触发预警",
                    String.format("当前访问次数：%d，预警阈值：%d", count, WARN_COUNT),
                    ip
            );
            log.warn("用户访问频率过高，userId={}, count={}", loginUserId, count);
        }
    }

    /**
     * listQuestionVOByPage 降级操作：直接返回本地数据
     */
    public BaseResponse<Page<QuestionVO>> handleFallback(QuestionQueryRequest questionQueryRequest,
                                                         HttpServletRequest request, Throwable ex) {
        // 可以返回本地数据或空数据
        return ResultUtils.success(new Page<>());
    }

    private List<AiGeneratedQuestion> parseAiGeneratedQuestions(String rawContent) {
        String jsonText = normalizeAiJson(rawContent);
        if (jsonText.startsWith("[")) {
            return JSONUtil.toList(jsonText, AiGeneratedQuestion.class);
        }
        if (jsonText.startsWith("{")) {
            JSONObject jsonObject = JSONUtil.parseObj(jsonText);
            JSONArray questions = jsonObject.getJSONArray("questions");
            ThrowUtils.throwIf(questions == null || questions.isEmpty(), ErrorCode.SYSTEM_ERROR, "AI 未返回题目数组");
            return JSONUtil.toList(questions, AiGeneratedQuestion.class);
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回内容不是合法 JSON");
    }

    private String normalizeAiJson(String rawContent) {
        String content = StringUtils.trimToEmpty(rawContent);
        if (content.startsWith("```")) {
            int firstLineBreak = content.indexOf('\n');
            if (firstLineBreak > -1) {
                content = content.substring(firstLineBreak + 1);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.lastIndexOf("```"));
            }
        }
        return content.trim();
    }

    @Data
    private static class AiGeneratedQuestion {
        private String title;
        private String content;
        private List<String> tags;
        private String answer;
    }

}
