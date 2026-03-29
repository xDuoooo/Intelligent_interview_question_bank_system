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
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 模拟面试服务实现
 */
@Service
public class MockInterviewServiceImpl extends ServiceImpl<MockInterviewMapper, MockInterview>
        implements MockInterviewService {

    private static final int DEFAULT_ROUNDS = 5;
    private static final int MIN_ROUNDS = 3;
    private static final int MAX_ROUNDS = 8;

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
        ThrowUtils.throwIf(StringUtils.length(mockInterview.getInterviewType()) > 40, ErrorCode.PARAMS_ERROR, "面试类型过长");
        ThrowUtils.throwIf(StringUtils.length(mockInterview.getTechStack()) > 256, ErrorCode.PARAMS_ERROR, "技术方向过长");
        ThrowUtils.throwIf(StringUtils.length(mockInterview.getResumeText()) > 4000, ErrorCode.PARAMS_ERROR, "简历/项目背景过长");
        ThrowUtils.throwIf(StringUtils.length(mockInterview.getDifficulty()) > 40, ErrorCode.PARAMS_ERROR, "难度描述过长");
        if (mockInterview.getExpectedRounds() == null) {
            mockInterview.setExpectedRounds(DEFAULT_ROUNDS);
        }
        ThrowUtils.throwIf(mockInterview.getExpectedRounds() < MIN_ROUNDS || mockInterview.getExpectedRounds() > MAX_ROUNDS,
                ErrorCode.PARAMS_ERROR, "面试轮次需在 3 到 8 轮之间");
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
        queryWrapper.like(StringUtils.isNotBlank(queryRequest.getInterviewType()), "interviewType", queryRequest.getInterviewType());
        queryWrapper.like(StringUtils.isNotBlank(queryRequest.getTechStack()), "techStack", queryRequest.getTechStack());
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
                int firstRound = 1;
                messageList.add(new InterviewMessage(
                        buildOpeningIntro(mockInterview),
                        true,
                        System.currentTimeMillis(),
                        0,
                        "opening"
                ));
                String openingQuestion = buildOpeningQuestion(mockInterview);
                messageList.add(new InterviewMessage(openingQuestion, true, System.currentTimeMillis() + 1, firstRound, "question"));
                mockInterview.setStatus(1);
                mockInterview.setCurrentRound(0);
                mockInterview.setExpectedRounds(normalizeExpectedRounds(mockInterview.getExpectedRounds()));
                mockInterview.setMessages(JSONUtil.toJsonStr(messageList));
                mockInterview.setReport(JSONUtil.toJsonStr(initReport(mockInterview)));
                this.updateById(mockInterview);
                return openingQuestion;
            case "chat":
                ThrowUtils.throwIf(mockInterview.getStatus() == null || mockInterview.getStatus() == 0,
                        ErrorCode.OPERATION_ERROR, "请先开始面试");
                ThrowUtils.throwIf(mockInterview.getStatus() == 2, ErrorCode.OPERATION_ERROR, "当前面试已结束");
                ThrowUtils.throwIf(StringUtils.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "回答不能为空");

                int answerRound = getNextRoundNumber(messageList);
                String latestQuestion = getLatestQuestion(messageList);
                messageList.add(new InterviewMessage(userMessage.trim(), false, System.currentTimeMillis(), answerRound, "answer"));

                InterviewReport interviewReport = parseReport(mockInterview.getReport(), mockInterview.getExpectedRounds());
                RoundAnalysis roundAnalysis = buildRoundAnalysis(mockInterview, messageList, answerRound);
                interviewReport.getRoundRecords().add(new RoundRecord(
                        answerRound,
                        latestQuestion,
                        userMessage.trim(),
                        roundAnalysis.getShortComment(),
                        roundAnalysis.getFocus(),
                        roundAnalysis.getScore(),
                        roundAnalysis.getCommunicationScore(),
                        roundAnalysis.getTechnicalScore(),
                        roundAnalysis.getProblemSolvingScore()
                ));
                interviewReport.setCompletedRounds(answerRound);
                mockInterview.setCurrentRound(answerRound);

                boolean shouldFinish = answerRound >= getExpectedRounds(mockInterview) || roundAnalysis.isShouldFinish();
                if (shouldFinish) {
                    SummaryResult summaryResult = buildSummary(mockInterview, messageList, interviewReport);
                    fillInterviewReportFromSummary(interviewReport, summaryResult);
                    String finalMessage = summaryResult.getDisplayText();
                    messageList.add(new InterviewMessage(finalMessage, true, System.currentTimeMillis() + 1, answerRound, "summary"));
                    mockInterview.setStatus(2);
                    mockInterview.setMessages(JSONUtil.toJsonStr(messageList));
                    mockInterview.setReport(JSONUtil.toJsonStr(interviewReport));
                    this.updateById(mockInterview);
                    return finalMessage;
                }

                String nextQuestion = roundAnalysis.getNextQuestion();
                messageList.add(new InterviewMessage(nextQuestion, true, System.currentTimeMillis() + 1, answerRound + 1, "question"));
                mockInterview.setMessages(JSONUtil.toJsonStr(messageList));
                mockInterview.setReport(JSONUtil.toJsonStr(interviewReport));
                this.updateById(mockInterview);
                return nextQuestion;
            case "end":
                ThrowUtils.throwIf(mockInterview.getStatus() != null && mockInterview.getStatus() == 2,
                        ErrorCode.OPERATION_ERROR, "当前面试已结束");
                InterviewReport endReport = parseReport(mockInterview.getReport(), mockInterview.getExpectedRounds());
                endReport.setCompletedRounds((int) countCandidateAnswers(messageList));
                messageList.add(new InterviewMessage("我想先结束这场面试，请给我最终反馈。", false, System.currentTimeMillis(), endReport.getCompletedRounds(), "end"));
                SummaryResult summaryResult = buildSummary(mockInterview, messageList, endReport);
                fillInterviewReportFromSummary(endReport, summaryResult);
                messageList.add(new InterviewMessage(summaryResult.getDisplayText(), true, System.currentTimeMillis() + 1, endReport.getCompletedRounds(), "summary"));
                mockInterview.setStatus(2);
                mockInterview.setCurrentRound(endReport.getCompletedRounds());
                mockInterview.setMessages(JSONUtil.toJsonStr(messageList));
                mockInterview.setReport(JSONUtil.toJsonStr(endReport));
                this.updateById(mockInterview);
                return summaryResult.getDisplayText();
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的事件类型");
        }
    }

    private void fillInterviewReportFromSummary(InterviewReport interviewReport, SummaryResult summaryResult) {
        interviewReport.setOverallScore(summaryResult.getOverallScore());
        interviewReport.setSummary(summaryResult.getSummary());
        interviewReport.setStrengths(summaryResult.getStrengths());
        interviewReport.setImprovements(summaryResult.getImprovements());
        interviewReport.setSuggestedTopics(summaryResult.getSuggestedTopics());
        interviewReport.setCommunicationScore(summaryResult.getCommunicationScore());
        interviewReport.setTechnicalScore(summaryResult.getTechnicalScore());
        interviewReport.setProblemSolvingScore(summaryResult.getProblemSolvingScore());
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

    private InterviewReport parseReport(String report, Integer expectedRounds) {
        if (StringUtils.isBlank(report)) {
            return initReport(expectedRounds);
        }
        try {
            InterviewReport interviewReport = JSONUtil.toBean(report, InterviewReport.class);
            if (interviewReport.getRoundRecords() == null) {
                interviewReport.setRoundRecords(new ArrayList<>());
            }
            if (interviewReport.getStrengths() == null) {
                interviewReport.setStrengths(new ArrayList<>());
            }
            if (interviewReport.getImprovements() == null) {
                interviewReport.setImprovements(new ArrayList<>());
            }
            if (interviewReport.getSuggestedTopics() == null) {
                interviewReport.setSuggestedTopics(new ArrayList<>());
            }
            if (interviewReport.getExpectedRounds() == null || interviewReport.getExpectedRounds() <= 0) {
                interviewReport.setExpectedRounds(normalizeExpectedRounds(expectedRounds));
            }
            return interviewReport;
        } catch (Exception e) {
            return initReport(expectedRounds);
        }
    }

    private long countCandidateAnswers(List<InterviewMessage> messageList) {
        return messageList.stream()
                .filter(message -> !message.isAI)
                .filter(message -> !"end".equals(message.stage))
                .count();
    }

    private String buildOpeningIntro(MockInterview mockInterview) {
        return String.format("你好，欢迎参加这场 %s 模拟面试。岗位是 %s，我会围绕你的项目经历和技术能力逐步追问。",
                StringUtils.defaultIfBlank(mockInterview.getInterviewType(), "技术"),
                StringUtils.defaultIfBlank(mockInterview.getJobPosition(), "目标岗位"));
    }

    private String buildOpeningQuestion(MockInterview mockInterview) {
        String systemPrompt = "你是一位专业的技术面试官。请像真实面试一样提出第一道问题。"
                + "输出必须是一句自然的面试问题，不要输出 markdown，不要附带答案。";
        String userPrompt = String.format(
                "岗位：%s；工作年限：%s；难度：%s；面试类型：%s；技术方向：%s；候选人背景：%s。请给出第一道高质量问题。",
                StringUtils.defaultIfBlank(mockInterview.getJobPosition(), "技术岗位"),
                StringUtils.defaultIfBlank(mockInterview.getWorkExperience(), "不限"),
                StringUtils.defaultIfBlank(mockInterview.getDifficulty(), "中等"),
                StringUtils.defaultIfBlank(mockInterview.getInterviewType(), "技术深挖"),
                StringUtils.defaultIfBlank(mockInterview.getTechStack(), "通用后端"),
                StringUtils.defaultIfBlank(mockInterview.getResumeText(), "暂无额外背景"));
        return chatWithFallback(systemPrompt, userPrompt, buildOpeningFallback(mockInterview));
    }

    private RoundAnalysis buildRoundAnalysis(MockInterview mockInterview, List<InterviewMessage> messageList, int completedRounds) {
        String systemPrompt = "你是一位专业的技术面试官。请严格输出 JSON 对象，不要输出 markdown。"
                + "字段必须包含：shortComment、focus、score、communicationScore、technicalScore、problemSolvingScore、nextQuestion、shouldFinish。";
        String userPrompt = String.format(
                "岗位：%s；工作年限：%s；难度：%s；面试类型：%s；技术方向：%s；计划轮次：%d；当前已完成轮次：%d；候选人背景：%s。"
                        + "请根据以下对话，对候选人最新回答做专业判断，并决定下一问。\n%s",
                StringUtils.defaultIfBlank(mockInterview.getJobPosition(), "技术岗位"),
                StringUtils.defaultIfBlank(mockInterview.getWorkExperience(), "不限"),
                StringUtils.defaultIfBlank(mockInterview.getDifficulty(), "中等"),
                StringUtils.defaultIfBlank(mockInterview.getInterviewType(), "技术深挖"),
                StringUtils.defaultIfBlank(mockInterview.getTechStack(), "通用后端"),
                getExpectedRounds(mockInterview),
                completedRounds,
                StringUtils.defaultIfBlank(mockInterview.getResumeText(), "暂无额外背景"),
                buildTranscript(messageList));
        String fallbackJson = JSONUtil.toJsonStr(buildRoundFallback(mockInterview, completedRounds));
        return parseRoundAnalysis(chatWithFallback(systemPrompt, userPrompt, fallbackJson), mockInterview, completedRounds);
    }

    private SummaryResult buildSummary(MockInterview mockInterview, List<InterviewMessage> messageList, InterviewReport interviewReport) {
        String systemPrompt = "你是一位专业技术面试官。请严格输出 JSON 对象，不要输出 markdown。"
                + "字段必须包含：overallScore、summary、strengths、improvements、suggestedTopics、communicationScore、technicalScore、problemSolvingScore、displayText。"
                + "displayText 必须以【面试结束】开头。";
        String userPrompt = String.format(
                "岗位：%s；工作年限：%s；难度：%s；面试类型：%s；技术方向：%s；计划轮次：%d；实际完成轮次：%d；候选人背景：%s。"
                        + "请总结以下面试记录，并结合已有轮次评语给出结构化终评。\n轮次评语：%s\n对话记录：\n%s",
                StringUtils.defaultIfBlank(mockInterview.getJobPosition(), "技术岗位"),
                StringUtils.defaultIfBlank(mockInterview.getWorkExperience(), "不限"),
                StringUtils.defaultIfBlank(mockInterview.getDifficulty(), "中等"),
                StringUtils.defaultIfBlank(mockInterview.getInterviewType(), "技术深挖"),
                StringUtils.defaultIfBlank(mockInterview.getTechStack(), "通用后端"),
                getExpectedRounds(mockInterview),
                interviewReport.getCompletedRounds(),
                StringUtils.defaultIfBlank(mockInterview.getResumeText(), "暂无额外背景"),
                JSONUtil.toJsonStr(interviewReport.getRoundRecords()),
                buildTranscript(messageList));
        String fallback = JSONUtil.toJsonStr(buildSummaryFallback(interviewReport));
        return parseSummaryResult(chatWithFallback(systemPrompt, userPrompt, fallback), interviewReport);
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

    private InterviewReport initReport(MockInterview mockInterview) {
        return initReport(mockInterview == null ? null : mockInterview.getExpectedRounds());
    }

    private InterviewReport initReport(Integer expectedRounds) {
        InterviewReport report = new InterviewReport();
        report.setExpectedRounds(normalizeExpectedRounds(expectedRounds));
        report.setCompletedRounds(0);
        report.setOverallScore(0);
        report.setSummary("");
        report.setCommunicationScore(0);
        report.setTechnicalScore(0);
        report.setProblemSolvingScore(0);
        report.setStrengths(new ArrayList<>());
        report.setImprovements(new ArrayList<>());
        report.setSuggestedTopics(new ArrayList<>());
        report.setRoundRecords(new ArrayList<>());
        return report;
    }

    private int normalizeExpectedRounds(Integer expectedRounds) {
        if (expectedRounds == null) {
            return DEFAULT_ROUNDS;
        }
        return Math.min(MAX_ROUNDS, Math.max(MIN_ROUNDS, expectedRounds));
    }

    private int getExpectedRounds(MockInterview mockInterview) {
        int expectedRounds = normalizeExpectedRounds(mockInterview.getExpectedRounds());
        mockInterview.setExpectedRounds(expectedRounds);
        return expectedRounds;
    }

    private int getNextRoundNumber(List<InterviewMessage> messageList) {
        return (int) countCandidateAnswers(messageList) + 1;
    }

    private String getLatestQuestion(List<InterviewMessage> messageList) {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            InterviewMessage interviewMessage = messageList.get(i);
            if (interviewMessage.isAI && "question".equals(interviewMessage.stage)) {
                return interviewMessage.content;
            }
        }
        return "";
    }

    private String buildOpeningFallback(MockInterview mockInterview) {
        String interviewType = StringUtils.defaultIfBlank(mockInterview.getInterviewType(), "技术深挖");
        if ("项目拷打".equals(interviewType)) {
            return "先挑一个你最有代表性的项目，讲清楚它的业务目标、你的职责以及你认为最难的一次技术决策。";
        }
        if ("系统设计".equals(interviewType)) {
            return "我们先做一道系统设计题。请你设计一个支持高并发的消息通知系统，并先说说你的总体思路。";
        }
        if ("HR".equals(interviewType)) {
            return "先做一个简短自我介绍，并说说你为什么会投递这个岗位。";
        }
        return String.format("请先做一个简短自我介绍，并说明你在 %s 相关项目中最有代表性的一次实践。",
                StringUtils.defaultIfBlank(mockInterview.getJobPosition(), "当前岗位"));
    }

    private RoundAnalysis buildRoundFallback(MockInterview mockInterview, int completedRounds) {
        RoundAnalysis roundAnalysis = new RoundAnalysis();
        roundAnalysis.setShortComment(completedRounds <= 2 ? "回答方向基本正确，但还可以再深入一些细节。" : "回答有一定深度，建议继续补充边界场景和量化结果。");
        roundAnalysis.setFocus("继续深挖项目细节、技术取舍和稳定性思路");
        roundAnalysis.setScore(Math.max(60, Math.min(88, 66 + completedRounds * 4)));
        roundAnalysis.setCommunicationScore(Math.max(58, Math.min(86, 64 + completedRounds * 3)));
        roundAnalysis.setTechnicalScore(Math.max(60, Math.min(90, 65 + completedRounds * 4)));
        roundAnalysis.setProblemSolvingScore(Math.max(58, Math.min(88, 63 + completedRounds * 4)));
        roundAnalysis.setNextQuestion(String.format("如果让你继续优化刚才提到的 %s 场景，你会从系统设计、性能、稳定性和异常处理几个方面分别怎么做？",
                StringUtils.defaultIfBlank(mockInterview.getTechStack(), mockInterview.getJobPosition())));
        roundAnalysis.setShouldFinish(false);
        return roundAnalysis;
    }

    private SummaryResult buildSummaryFallback(InterviewReport interviewReport) {
        SummaryResult summaryResult = new SummaryResult();
        int completedRounds = Math.max(1, interviewReport.getCompletedRounds());
        summaryResult.setOverallScore(averageRoundScore(interviewReport, RoundRecord::getScore, 78));
        summaryResult.setSummary("整体表现较稳，具备一定项目经验和表达基础，但在量化结果、系统设计取舍与边界场景分析上还有提升空间。");
        summaryResult.setStrengths(new ArrayList<>(List.of("能够围绕问题作答", "具备一定项目实践经验", "回答结构相对完整")));
        summaryResult.setImprovements(new ArrayList<>(List.of("补充更多量化结果", "进一步说明设计取舍与稳定性策略", "回答时增加边界场景和异常处理说明")));
        summaryResult.setSuggestedTopics(new ArrayList<>(List.of("高并发场景设计", "数据库与缓存优化", "异常处理与稳定性治理")));
        summaryResult.setCommunicationScore(Math.max(60, Math.min(90, 68 + completedRounds * 2)));
        summaryResult.setTechnicalScore(Math.max(62, Math.min(92, 70 + completedRounds * 2)));
        summaryResult.setProblemSolvingScore(Math.max(60, Math.min(90, 67 + completedRounds * 2)));
        summaryResult.setDisplayText("【面试结束】总体评价：回答较完整，具备一定技术基础。亮点：能结合项目经验展开说明。改进建议：继续补充量化结果、设计取舍和异常场景细节。");
        return summaryResult;
    }

    private RoundAnalysis parseRoundAnalysis(String content, MockInterview mockInterview, int completedRounds) {
        try {
            Map<?, ?> result = JSONUtil.toBean(extractJsonContent(content), Map.class);
            RoundAnalysis fallback = buildRoundFallback(mockInterview, completedRounds);
            RoundAnalysis roundAnalysis = new RoundAnalysis();
            roundAnalysis.setShortComment(getString(result.get("shortComment"), fallback.getShortComment()));
            roundAnalysis.setFocus(getString(result.get("focus"), fallback.getFocus()));
            roundAnalysis.setScore(normalizeScore(result.get("score"), fallback.getScore()));
            roundAnalysis.setCommunicationScore(normalizeScore(result.get("communicationScore"), fallback.getCommunicationScore()));
            roundAnalysis.setTechnicalScore(normalizeScore(result.get("technicalScore"), fallback.getTechnicalScore()));
            roundAnalysis.setProblemSolvingScore(normalizeScore(result.get("problemSolvingScore"), fallback.getProblemSolvingScore()));
            roundAnalysis.setNextQuestion(getString(result.get("nextQuestion"), fallback.getNextQuestion()));
            roundAnalysis.setShouldFinish(Boolean.parseBoolean(String.valueOf(result.get("shouldFinish"))));
            return roundAnalysis;
        } catch (Exception e) {
            return buildRoundFallback(mockInterview, completedRounds);
        }
    }

    private SummaryResult parseSummaryResult(String content, InterviewReport interviewReport) {
        try {
            Map<?, ?> result = JSONUtil.toBean(extractJsonContent(content), Map.class);
            SummaryResult fallback = buildSummaryFallback(interviewReport);
            SummaryResult summaryResult = new SummaryResult();
            summaryResult.setOverallScore(normalizeScore(result.get("overallScore"), fallback.getOverallScore()));
            summaryResult.setSummary(getString(result.get("summary"), fallback.getSummary()));
            summaryResult.setStrengths(getStringList(result.get("strengths"), fallback.getStrengths()));
            summaryResult.setImprovements(getStringList(result.get("improvements"), fallback.getImprovements()));
            summaryResult.setSuggestedTopics(getStringList(result.get("suggestedTopics"), fallback.getSuggestedTopics()));
            summaryResult.setCommunicationScore(normalizeScore(result.get("communicationScore"), fallback.getCommunicationScore()));
            summaryResult.setTechnicalScore(normalizeScore(result.get("technicalScore"), fallback.getTechnicalScore()));
            summaryResult.setProblemSolvingScore(normalizeScore(result.get("problemSolvingScore"), fallback.getProblemSolvingScore()));
            summaryResult.setDisplayText(getString(result.get("displayText"), fallback.getDisplayText()));
            if (!summaryResult.getDisplayText().startsWith("【面试结束】")) {
                summaryResult.setDisplayText("【面试结束】" + summaryResult.getDisplayText());
            }
            return summaryResult;
        } catch (Exception e) {
            return buildSummaryFallback(interviewReport);
        }
    }

    private String extractJsonContent(String content) {
        String text = StringUtils.trimToEmpty(content);
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*", "").trim();
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3).trim();
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String getString(Object value, String defaultValue) {
        String text = value == null ? null : String.valueOf(value).trim();
        return StringUtils.isBlank(text) ? defaultValue : text;
    }

    private List<String> getStringList(Object value, List<String> defaultValue) {
        if (value instanceof List<?> list && !list.isEmpty()) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                String text = item == null ? null : String.valueOf(item).trim();
                if (StringUtils.isNotBlank(text)) {
                    result.add(text);
                }
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        return new ArrayList<>(defaultValue == null ? Collections.emptyList() : defaultValue);
    }

    private int normalizeScore(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return Math.max(0, Math.min(100, number.intValue()));
        }
        try {
            return Math.max(0, Math.min(100, Integer.parseInt(String.valueOf(value))));
        } catch (Exception e) {
            return Math.max(0, Math.min(100, defaultValue));
        }
    }

    private int averageRoundScore(InterviewReport report, Function<RoundRecord, Integer> getter, int defaultValue) {
        List<RoundRecord> roundRecords = report == null ? Collections.emptyList() : report.getRoundRecords();
        int sum = 0;
        int count = 0;
        for (RoundRecord roundRecord : roundRecords) {
            Integer score = getter.apply(roundRecord);
            if (score != null) {
                sum += score;
                count++;
            }
        }
        return count == 0 ? defaultValue : Math.max(0, Math.min(100, sum / count));
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
        private Integer round;
        private String stage;
    }

    @Data
    @NoArgsConstructor
    private static class RoundAnalysis {
        private String shortComment;
        private String focus;
        private Integer score;
        private Integer communicationScore;
        private Integer technicalScore;
        private Integer problemSolvingScore;
        private String nextQuestion;
        private boolean shouldFinish;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RoundRecord {
        private Integer round;
        private String question;
        private String answer;
        private String shortComment;
        private String focus;
        private Integer score;
        private Integer communicationScore;
        private Integer technicalScore;
        private Integer problemSolvingScore;
    }

    @Data
    @NoArgsConstructor
    private static class InterviewReport {
        private Integer expectedRounds;
        private Integer completedRounds;
        private Integer overallScore;
        private String summary;
        private Integer communicationScore;
        private Integer technicalScore;
        private Integer problemSolvingScore;
        private List<String> strengths;
        private List<String> improvements;
        private List<String> suggestedTopics;
        private List<RoundRecord> roundRecords;
    }

    @Data
    @NoArgsConstructor
    private static class SummaryResult {
        private Integer overallScore;
        private String summary;
        private Integer communicationScore;
        private Integer technicalScore;
        private Integer problemSolvingScore;
        private List<String> strengths;
        private List<String> improvements;
        private List<String> suggestedTopics;
        private String displayText;
    }
}
