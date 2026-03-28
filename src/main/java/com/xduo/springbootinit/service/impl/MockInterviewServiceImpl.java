package com.xduo.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.constant.CommonConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.manager.AiManager;
import com.xduo.springbootinit.mapper.MockInterviewMapper;
import com.xduo.springbootinit.model.dto.mockinterview.MockInterviewQueryRequest;
import com.xduo.springbootinit.model.entity.MockInterview;
import com.xduo.springbootinit.service.MockInterviewService;
import com.xduo.springbootinit.utils.SqlUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 模拟面试服务实现
 */
@Service
public class MockInterviewServiceImpl extends ServiceImpl<MockInterviewMapper, MockInterview>
        implements MockInterviewService {

    private static final int MAX_ROUNDS = 5;

    @Resource
    private AiManager aiManager;

    @Override
    public void validMockInterview(MockInterview mockInterview, boolean add) {
        ThrowUtils.throwIf(mockInterview == null, ErrorCode.PARAMS_ERROR);
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(mockInterview.getJobPosition()), ErrorCode.PARAMS_ERROR, "岗位不能为空");
        }
        ThrowUtils.throwIf(StringUtils.length(mockInterview.getJobPosition()) > 80, ErrorCode.PARAMS_ERROR, "岗位过长");
        ThrowUtils.throwIf(StringUtils.length(mockInterview.getWorkExperience()) > 40, ErrorCode.PARAMS_ERROR, "工作年限过长");
        ThrowUtils.throwIf(StringUtils.length(mockInterview.getDifficulty()) > 40, ErrorCode.PARAMS_ERROR, "难度描述过长");
    }

    @Override
    public QueryWrapper<MockInterview> getQueryWrapper(MockInterviewQueryRequest queryRequest) {
        QueryWrapper<MockInterview> queryWrapper = new QueryWrapper<>();
        if (queryRequest == null) {
            return queryWrapper;
        }
        queryWrapper.eq(queryRequest.getId() != null && queryRequest.getId() > 0, "id", queryRequest.getId());
        queryWrapper.eq(queryRequest.getUserId() != null && queryRequest.getUserId() > 0, "userId", queryRequest.getUserId());
        queryWrapper.eq(queryRequest.getStatus() != null, "status", queryRequest.getStatus());
        queryWrapper.like(StringUtils.isNotBlank(queryRequest.getJobPosition()), "jobPosition", queryRequest.getJobPosition());
        queryWrapper.like(StringUtils.isNotBlank(queryRequest.getWorkExperience()), "workExperience", queryRequest.getWorkExperience());
        queryWrapper.like(StringUtils.isNotBlank(queryRequest.getDifficulty()), "difficulty", queryRequest.getDifficulty());
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                sortField);
        return queryWrapper;
    }

    @Override
    public String handleInterviewEvent(MockInterview mockInterview, String event, String userMessage) {
        ThrowUtils.throwIf(mockInterview == null || mockInterview.getId() == null, ErrorCode.PARAMS_ERROR);
        String eventType = StringUtils.defaultIfBlank(event, "chat");
        List<InterviewMessage> messageList = parseMessages(mockInterview.getMessages());

        switch (eventType) {
            case "start":
                ThrowUtils.throwIf(mockInterview.getStatus() != null && mockInterview.getStatus() != 0,
                        ErrorCode.OPERATION_ERROR, "面试已经开始");
                messageList.add(new InterviewMessage("面试开始", false, System.currentTimeMillis()));
                String openingQuestion = buildOpeningQuestion(mockInterview);
                messageList.add(new InterviewMessage(openingQuestion, true, System.currentTimeMillis() + 1));
                mockInterview.setStatus(1);
                mockInterview.setMessages(JSONUtil.toJsonStr(messageList));
                this.updateById(mockInterview);
                return openingQuestion;
            case "chat":
                ThrowUtils.throwIf(mockInterview.getStatus() == null || mockInterview.getStatus() == 0,
                        ErrorCode.OPERATION_ERROR, "请先开始面试");
                ThrowUtils.throwIf(mockInterview.getStatus() == 2, ErrorCode.OPERATION_ERROR, "当前面试已结束");
                ThrowUtils.throwIf(StringUtils.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "回答不能为空");
                messageList.add(new InterviewMessage(userMessage.trim(), false, System.currentTimeMillis()));
                long answerCount = countCandidateAnswers(messageList);
                boolean shouldFinish = answerCount >= MAX_ROUNDS;
                String followUp = shouldFinish ? buildSummary(mockInterview, messageList) : buildFollowUpQuestion(mockInterview, messageList);
                messageList.add(new InterviewMessage(followUp, true, System.currentTimeMillis() + 1));
                if (shouldFinish || followUp.contains("【面试结束】")) {
                    mockInterview.setStatus(2);
                }
                mockInterview.setMessages(JSONUtil.toJsonStr(messageList));
                this.updateById(mockInterview);
                return followUp;
            case "end":
                ThrowUtils.throwIf(mockInterview.getStatus() != null && mockInterview.getStatus() == 2,
                        ErrorCode.OPERATION_ERROR, "当前面试已结束");
                messageList.add(new InterviewMessage("面试结束", false, System.currentTimeMillis()));
                String summary = buildSummary(mockInterview, messageList);
                messageList.add(new InterviewMessage(summary, true, System.currentTimeMillis() + 1));
                mockInterview.setStatus(2);
                mockInterview.setMessages(JSONUtil.toJsonStr(messageList));
                this.updateById(mockInterview);
                return summary;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的事件类型");
        }
    }

    private List<InterviewMessage> parseMessages(String messages) {
        if (StringUtils.isBlank(messages)) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.toList(messages, InterviewMessage.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private long countCandidateAnswers(List<InterviewMessage> messageList) {
        return messageList.stream()
                .filter(message -> !message.isAI)
                .filter(message -> !"面试开始".equals(message.content))
                .filter(message -> !"面试结束".equals(message.content))
                .count();
    }

    private String buildOpeningQuestion(MockInterview mockInterview) {
        String systemPrompt = "你是一位专业的技术面试官。请直接输出第一道面试问题，不要加开场白，不要使用 markdown。";
        String userPrompt = String.format("岗位：%s；工作年限：%s；难度：%s。请给出一道高质量的开场问题。",
                StringUtils.defaultIfBlank(mockInterview.getJobPosition(), "技术岗位"),
                StringUtils.defaultIfBlank(mockInterview.getWorkExperience(), "不限"),
                StringUtils.defaultIfBlank(mockInterview.getDifficulty(), "中等"));
        return chatWithFallback(systemPrompt, userPrompt,
                String.format("请先做一个简短自我介绍，并说明你在 %s 相关项目中最有代表性的一次实践。", mockInterview.getJobPosition()));
    }

    private String buildFollowUpQuestion(MockInterview mockInterview, List<InterviewMessage> messageList) {
        String systemPrompt = "你是一位专业的技术面试官。请根据候选人的上一轮回答继续追问。输出一段简洁的问题，不要输出答案。";
        String userPrompt = String.format("岗位：%s；工作年限：%s；难度：%s。以下是当前对话，请继续追问一题：\n%s",
                StringUtils.defaultIfBlank(mockInterview.getJobPosition(), "技术岗位"),
                StringUtils.defaultIfBlank(mockInterview.getWorkExperience(), "不限"),
                StringUtils.defaultIfBlank(mockInterview.getDifficulty(), "中等"),
                buildTranscript(messageList));
        String fallback = String.format("如果让你继续优化刚才提到的 %s 场景，你会从系统设计、性能和异常处理三个方面分别怎么做？",
                StringUtils.defaultIfBlank(mockInterview.getJobPosition(), "技术"));
        return chatWithFallback(systemPrompt, userPrompt, fallback);
    }

    private String buildSummary(MockInterview mockInterview, List<InterviewMessage> messageList) {
        String systemPrompt = "你是一位专业技术面试官。请基于对话给出简洁的总结评价。"
                + "必须以【面试结束】开头，并给出：总体评价、亮点、改进建议。";
        String userPrompt = String.format("岗位：%s；工作年限：%s；难度：%s。请总结以下面试记录：\n%s",
                StringUtils.defaultIfBlank(mockInterview.getJobPosition(), "技术岗位"),
                StringUtils.defaultIfBlank(mockInterview.getWorkExperience(), "不限"),
                StringUtils.defaultIfBlank(mockInterview.getDifficulty(), "中等"),
                buildTranscript(messageList));
        String fallback = "【面试结束】总体评价：基础知识较扎实，表达较完整。亮点：能够结合实际项目回答问题。改进建议：回答中可以补充更多量化结果，并进一步说明高并发、异常兜底和性能优化细节。";
        return chatWithFallback(systemPrompt, userPrompt, fallback);
    }

    private String buildTranscript(List<InterviewMessage> messageList) {
        StringBuilder transcriptBuilder = new StringBuilder();
        for (InterviewMessage message : messageList) {
            transcriptBuilder.append(message.isAI ? "面试官：" : "候选人：")
                    .append(message.content)
                    .append('\n');
        }
        return transcriptBuilder.toString();
    }

    private String chatWithFallback(String systemPrompt, String userPrompt, String fallback) {
        try {
            String content = aiManager.doChat(systemPrompt, userPrompt);
            return StringUtils.isBlank(content) ? fallback : content.trim();
        } catch (Exception e) {
            return fallback;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class InterviewMessage {
        private String content;
        private boolean isAI;
        private long timestamp;
    }
}
