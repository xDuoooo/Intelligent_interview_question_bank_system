package com.xduo.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xduo.springbootinit.model.entity.AdminOperationLog;
import com.xduo.springbootinit.model.entity.MockInterview;
import com.xduo.springbootinit.model.entity.Question;
import com.xduo.springbootinit.model.entity.QuestionComment;
import com.xduo.springbootinit.model.entity.QuestionFavour;
import com.xduo.springbootinit.model.entity.QuestionSearchLog;
import com.xduo.springbootinit.model.entity.SecurityAlert;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.entity.UserQuestionHistory;
import com.xduo.springbootinit.service.AdminDashboardService;
import com.xduo.springbootinit.service.AdminOperationLogService;
import com.xduo.springbootinit.service.MockInterviewService;
import com.xduo.springbootinit.service.QuestionBankService;
import com.xduo.springbootinit.service.QuestionCommentService;
import com.xduo.springbootinit.service.QuestionFavourService;
import com.xduo.springbootinit.service.QuestionSearchLogService;
import com.xduo.springbootinit.service.QuestionService;
import com.xduo.springbootinit.service.SecurityAlertService;
import com.xduo.springbootinit.service.UserQuestionHistoryService;
import com.xduo.springbootinit.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理端数据驾驶舱服务实现
 */
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionBankService questionBankService;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionCommentService questionCommentService;

    @Resource
    private MockInterviewService mockInterviewService;

    @Resource
    private UserQuestionHistoryService userQuestionHistoryService;

    @Resource
    private AdminOperationLogService adminOperationLogService;

    @Resource
    private QuestionFavourService questionFavourService;

    @Resource
    private QuestionSearchLogService questionSearchLogService;

    @Resource
    private SecurityAlertService securityAlertService;

    @Override
    public Map<String, Object> getDashboardOverview() {
        List<Question> questionList = questionService.list();
        List<UserQuestionHistory> historyList = userQuestionHistoryService.list();
        List<QuestionComment> commentList = questionCommentService.list();
        List<MockInterview> mockInterviewList = mockInterviewService.list();
        List<QuestionSearchLog> searchLogList = questionSearchLogService.list();
        List<SecurityAlert> securityAlertList = securityAlertService.list();
        List<AdminOperationLog> operationLogList = listRecentOperations();

        Map<String, Object> overviewData = new HashMap<>();
        overviewData.put("overview", buildOverview(questionList, commentList, mockInterviewList, securityAlertList));
        overviewData.put("todayStats", buildTodayStats(historyList, commentList, mockInterviewList, securityAlertList));
        overviewData.put("trend", buildTrend(historyList, commentList));
        overviewData.put("searchAnalytics", buildSearchAnalytics(searchLogList));
        overviewData.put("tagDistribution", buildTagDistribution(questionList));
        overviewData.put("questionHealth", buildQuestionHealth(questionList, historyList));
        overviewData.put("riskAlerts", buildRiskAlerts(securityAlertList));
        overviewData.put("recentOperations", buildRecentOperations(operationLogList));
        return overviewData;
    }

    private Map<String, Object> buildOverview(List<Question> questionList, List<QuestionComment> commentList,
                                              List<MockInterview> mockInterviewList, List<SecurityAlert> securityAlertList) {
        Map<String, Object> overview = new HashMap<>();
        overview.put("userTotal", userService.count());
        overview.put("bankTotal", questionBankService.count());
        overview.put("questionTotal", questionList.size());
        overview.put("tagTotal", countDistinctTags(questionList));
        overview.put("commentTotal", commentList.size());
        overview.put("mockInterviewTotal", mockInterviewList.size());

        QueryWrapper<User> bannedUserWrapper = new QueryWrapper<>();
        bannedUserWrapper.eq("userRole", "ban");
        overview.put("bannedUserTotal", userService.count(bannedUserWrapper));

        QueryWrapper<QuestionFavour> favourWrapper = new QueryWrapper<>();
        overview.put("favourTotal", questionFavourService.count(favourWrapper));
        overview.put("pendingRiskAlertTotal", securityAlertList.stream()
                .filter(alert -> alert.getStatus() != null && alert.getStatus() == 0)
                .count());
        return overview;
    }

    private Map<String, Object> buildTodayStats(List<UserQuestionHistory> historyList, List<QuestionComment> commentList,
                                                List<MockInterview> mockInterviewList,
                                                List<SecurityAlert> securityAlertList) {
        Map<String, Object> todayStats = new HashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate sevenDaysAgo = today.minusDays(6);

        todayStats.put("todayRegisterCount", countUsersBetween(today, today));
        todayStats.put("sevenDayRegisterCount", countUsersBetween(sevenDaysAgo, today));
        todayStats.put("todayPracticeCount", countByDate(historyList, UserQuestionHistory::getUpdateTime, today));
        todayStats.put("sevenDayPracticeCount", countBetween(historyList, UserQuestionHistory::getUpdateTime, sevenDaysAgo, today));
        todayStats.put("todayCommentCount", countByDate(commentList, QuestionComment::getCreateTime, today));
        todayStats.put("todayMockInterviewCount", countByDate(mockInterviewList, MockInterview::getCreateTime, today));
        todayStats.put("todayRiskAlertCount", countByDate(securityAlertList, SecurityAlert::getCreateTime, today));
        todayStats.put("sevenDayActiveUserCount", historyList.stream()
                .filter(item -> toLocalDate(item.getUpdateTime()) != null)
                .filter(item -> !toLocalDate(item.getUpdateTime()).isBefore(sevenDaysAgo))
                .map(UserQuestionHistory::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet())
                .size());
        return todayStats;
    }

    private Map<String, Object> buildTrend(List<UserQuestionHistory> historyList, List<QuestionComment> commentList) {
        Map<String, Object> trend = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<Long> registerTrend = new ArrayList<>();
        List<Long> practiceTrend = new ArrayList<>();
        List<Long> commentTrend = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dateList.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            registerTrend.add(countUsersBetween(date, date));
            practiceTrend.add(countByDate(historyList, UserQuestionHistory::getUpdateTime, date));
            commentTrend.add(countByDate(commentList, QuestionComment::getCreateTime, date));
        }

        trend.put("dates", dateList);
        trend.put("registerTrend", registerTrend);
        trend.put("practiceTrend", practiceTrend);
        trend.put("commentTrend", commentTrend);
        return trend;
    }

    private List<Map<String, Object>> buildTagDistribution(List<Question> questionList) {
        Map<String, Long> tagCounter = new HashMap<>();
        for (Question question : questionList) {
            if (StringUtils.isBlank(question.getTags())) {
                continue;
            }
            try {
                List<String> tagList = JSONUtil.toList(question.getTags(), String.class);
                for (String tag : tagList) {
                    if (StringUtils.isBlank(tag)) {
                        continue;
                    }
                    tagCounter.merge(tag.trim(), 1L, Long::sum);
                }
            } catch (Exception ignored) {
            }
        }
        return tagCounter.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildQuestionHealth(List<Question> questionList, List<UserQuestionHistory> historyList) {
        Map<Long, Long> practiceCountMap = historyList.stream()
                .filter(item -> item.getQuestionId() != null)
                .collect(Collectors.groupingBy(UserQuestionHistory::getQuestionId, Collectors.counting()));

        return questionList.stream()
                .sorted(Comparator
                        .comparingLong((Question question) -> practiceCountMap.getOrDefault(question.getId(), 0L))
                        .thenComparing(Question::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(question -> {
                    long practiceCount = practiceCountMap.getOrDefault(question.getId(), 0L);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", question.getId());
                    item.put("title", question.getTitle());
                    item.put("practiceCount", practiceCount);
                    item.put("status", practiceCount == 0 ? "待激活" : practiceCount < 3 ? "低热度" : "正常");
                    item.put("updateTime", question.getUpdateTime());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildRecentOperations(List<AdminOperationLog> operationLogList) {
        return operationLogList.stream().map(log -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", log.getId());
            item.put("userName", log.getUserName());
            item.put("operation", log.getOperation());
            item.put("method", log.getMethod());
            item.put("ip", log.getIp());
            item.put("createTime", log.getCreateTime());
            return item;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> buildSearchAnalytics(List<QuestionSearchLog> searchLogList) {
        Map<String, Object> analytics = new HashMap<>();
        long totalSearchCount = searchLogList.size();
        long zeroResultSearchCount = searchLogList.stream()
                .filter(log -> log.getHasNoResult() != null && log.getHasNoResult() == 1)
                .count();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));

        analytics.put("totalSearchCount", totalSearchCount);
        analytics.put("todaySearchCount", countByDate(searchLogList, QuestionSearchLog::getCreateTime, today));
        analytics.put("distinctKeywordCount", searchLogList.stream()
                .map(QuestionSearchLog::getSearchText)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet())
                .size());
        analytics.put("zeroResultSearchCount", zeroResultSearchCount);
        analytics.put("zeroResultRate", totalSearchCount == 0 ? 0D : Math.round(zeroResultSearchCount * 10000D / totalSearchCount) / 100D);
        analytics.put("topKeywords", buildTopSearchKeywords(searchLogList, false));
        analytics.put("zeroResultKeywords", buildTopSearchKeywords(searchLogList, true));
        analytics.put("trend", buildSearchTrend(searchLogList));
        return analytics;
    }

    private List<Map<String, Object>> buildTopSearchKeywords(List<QuestionSearchLog> searchLogList, boolean onlyNoResult) {
        Map<String, SearchKeywordMetric> keywordMetricMap = new LinkedHashMap<>();
        searchLogList.stream()
                .filter(log -> StringUtils.isNotBlank(log.getSearchText()))
                .filter(log -> !onlyNoResult || (log.getHasNoResult() != null && log.getHasNoResult() == 1))
                .forEach(log -> {
                    String keyword = log.getSearchText().trim();
                    SearchKeywordMetric metric = keywordMetricMap.computeIfAbsent(keyword, key -> new SearchKeywordMetric());
                    metric.setKeyword(keyword);
                    metric.increaseCount();
                    if (log.getHasNoResult() != null && log.getHasNoResult() == 1) {
                        metric.increaseZeroResultCount();
                    }
                    metric.addResultCount(log.getResultCount() == null ? 0 : log.getResultCount());
                    metric.setLastSearchTime(log.getCreateTime());
                });

        return keywordMetricMap.values().stream()
                .sorted(Comparator
                        .comparingInt(SearchKeywordMetric::getCount).reversed()
                        .thenComparing(SearchKeywordMetric::getLastSearchTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(onlyNoResult ? 10 : 20)
                .map(metric -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("keyword", metric.getKeyword());
                    item.put("count", metric.getCount());
                    item.put("zeroResultCount", metric.getZeroResultCount());
                    item.put("avgResultCount", metric.getCount() == 0 ? 0D : Math.round(metric.getResultCountTotal() * 100D / metric.getCount()) / 100D);
                    item.put("lastSearchTime", metric.getLastSearchTime());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildSearchTrend(List<QuestionSearchLog> searchLogList) {
        Map<String, Object> trend = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<Long> searchTrend = new ArrayList<>();
        List<Long> zeroResultTrend = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dateList.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            searchTrend.add(countByDate(searchLogList, QuestionSearchLog::getCreateTime, date));
            zeroResultTrend.add(searchLogList.stream()
                    .filter(log -> toLocalDate(log.getCreateTime()) != null)
                    .filter(log -> date.equals(toLocalDate(log.getCreateTime())))
                    .filter(log -> log.getHasNoResult() != null && log.getHasNoResult() == 1)
                    .count());
        }

        trend.put("dates", dateList);
        trend.put("searchTrend", searchTrend);
        trend.put("zeroResultTrend", zeroResultTrend);
        return trend;
    }

    private List<Map<String, Object>> buildRiskAlerts(List<SecurityAlert> securityAlertList) {
        return securityAlertList.stream()
                .filter(alert -> alert.getStatus() != null && alert.getStatus() == 0)
                .sorted(Comparator.comparing(SecurityAlert::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(alert -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", alert.getId());
                    item.put("userId", alert.getUserId());
                    item.put("userName", alert.getUserName());
                    item.put("alertType", alert.getAlertType());
                    item.put("riskLevel", alert.getRiskLevel());
                    item.put("reason", alert.getReason());
                    item.put("detail", alert.getDetail());
                    item.put("ip", alert.getIp());
                    item.put("createTime", alert.getCreateTime());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<AdminOperationLog> listRecentOperations() {
        QueryWrapper<AdminOperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("createTime");
        queryWrapper.last("limit 6");
        return adminOperationLogService.list(queryWrapper);
    }

    private int countDistinctTags(List<Question> questionList) {
        Set<String> tagSet = new LinkedHashSet<>();
        for (Question question : questionList) {
            if (StringUtils.isBlank(question.getTags())) {
                continue;
            }
            try {
                tagSet.addAll(JSONUtil.toList(question.getTags(), String.class));
            } catch (Exception ignored) {
            }
        }
        return tagSet.size();
    }

    private long countUsersBetween(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("createTime", toDate(startDate, true), toDate(endDate, false));
        return userService.count(queryWrapper);
    }

    private <T> long countBetween(List<T> dataList, Function<T, Date> dateGetter, LocalDate startDate, LocalDate endDate) {
        return dataList.stream()
                .map(dateGetter)
                .map(this::toLocalDate)
                .filter(java.util.Objects::nonNull)
                .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                .count();
    }

    private <T> long countByDate(List<T> dataList, Function<T, Date> dateGetter, LocalDate targetDate) {
        return dataList.stream()
                .map(dateGetter)
                .map(this::toLocalDate)
                .filter(targetDate::equals)
                .count();
    }

    private static class SearchKeywordMetric {
        private String keyword;
        private int count;
        private int zeroResultCount;
        private long resultCountTotal;
        private Date lastSearchTime;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public int getCount() {
            return count;
        }

        public void increaseCount() {
            this.count++;
        }

        public int getZeroResultCount() {
            return zeroResultCount;
        }

        public void increaseZeroResultCount() {
            this.zeroResultCount++;
        }

        public long getResultCountTotal() {
            return resultCountTotal;
        }

        public void addResultCount(int resultCount) {
            this.resultCountTotal += resultCount;
        }

        public Date getLastSearchTime() {
            return lastSearchTime;
        }

        public void setLastSearchTime(Date lastSearchTime) {
            if (lastSearchTime == null) {
                return;
            }
            if (this.lastSearchTime == null || lastSearchTime.after(this.lastSearchTime)) {
                this.lastSearchTime = lastSearchTime;
            }
        }
    }

    private Date toDate(LocalDate localDate, boolean startOfDay) {
        LocalDateTime localDateTime = startOfDay ? localDate.atStartOfDay() : localDate.atTime(LocalTime.MAX);
        return Date.from(localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant());
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
    }
}
