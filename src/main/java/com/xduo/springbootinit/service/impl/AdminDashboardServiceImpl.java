package com.xduo.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xduo.springbootinit.model.entity.AdminOperationLog;
import com.xduo.springbootinit.model.entity.MockInterview;
import com.xduo.springbootinit.model.entity.Question;
import com.xduo.springbootinit.model.entity.QuestionComment;
import com.xduo.springbootinit.model.entity.QuestionFavour;
import com.xduo.springbootinit.model.entity.QuestionRecommendLog;
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
import com.xduo.springbootinit.service.QuestionRecommendLogService;
import com.xduo.springbootinit.service.QuestionSearchLogService;
import com.xduo.springbootinit.service.QuestionService;
import com.xduo.springbootinit.service.SecurityAlertService;
import com.xduo.springbootinit.service.UserQuestionHistoryService;
import com.xduo.springbootinit.service.UserService;
import com.xduo.springbootinit.utils.CityUtils;
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
    private QuestionRecommendLogService questionRecommendLogService;

    @Resource
    private SecurityAlertService securityAlertService;

    @Override
    public Map<String, Object> getDashboardOverview() {
        List<User> userList = listUserCitySnapshot();
        List<Question> questionList = listDashboardQuestions();
        Map<Long, Long> questionPracticeCountMap = listPracticeCountMapByQuestion();
        Map<Long, Long> userPracticeCountMap = listPracticeCountMapByUser();
        List<SecurityAlert> pendingSecurityAlertList = listPendingSecurityAlerts();
        List<AdminOperationLog> operationLogList = listRecentOperations();

        Map<String, Object> overviewData = new HashMap<>();
        overviewData.put("overview", buildOverview(questionList));
        overviewData.put("todayStats", buildTodayStats());
        overviewData.put("trend", buildTrend());
        overviewData.put("growthComparison", buildGrowthComparison());
        overviewData.put("retentionAnalytics", buildRetentionAnalytics());
        overviewData.put("searchAnalytics", buildSearchAnalytics());
        overviewData.put("recommendationAnalytics", buildRecommendationAnalytics());
        overviewData.put("geoDistribution", buildGeoDistribution(userList, userPracticeCountMap));
        overviewData.put("tagDistribution", buildTagDistribution(questionList));
        overviewData.put("questionHealth", buildQuestionHealth(questionList, questionPracticeCountMap));
        overviewData.put("riskAlerts", buildRiskAlerts(pendingSecurityAlertList));
        overviewData.put("recentOperations", buildRecentOperations(operationLogList));
        return overviewData;
    }

    private Map<String, Object> buildOverview(List<Question> questionList) {
        Map<String, Object> overview = new HashMap<>();
        overview.put("userTotal", userService.count());
        overview.put("bankTotal", questionBankService.count());
        overview.put("questionTotal", questionService.count());
        overview.put("tagTotal", countDistinctTags(questionList));
        overview.put("commentTotal", questionCommentService.count());
        overview.put("mockInterviewTotal", mockInterviewService.count());

        QueryWrapper<User> bannedUserWrapper = new QueryWrapper<>();
        bannedUserWrapper.eq("userRole", "ban");
        overview.put("bannedUserTotal", userService.count(bannedUserWrapper));

        overview.put("favourTotal", questionFavourService.count());

        QueryWrapper<SecurityAlert> pendingAlertWrapper = new QueryWrapper<>();
        pendingAlertWrapper.eq("status", 0);
        overview.put("pendingRiskAlertTotal", securityAlertService.count(pendingAlertWrapper));
        return overview;
    }

    private Map<String, Object> buildTodayStats() {
        Map<String, Object> todayStats = new HashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate sevenDaysAgo = today.minusDays(6);

        todayStats.put("todayRegisterCount", countUsersBetween(today, today));
        todayStats.put("sevenDayRegisterCount", countUsersBetween(sevenDaysAgo, today));
        todayStats.put("todayPracticeCount", countHistoryBetween(today, today));
        todayStats.put("sevenDayPracticeCount", countHistoryBetween(sevenDaysAgo, today));
        todayStats.put("todayCommentCount", countCommentsBetween(today, today));
        todayStats.put("todayMockInterviewCount", countMockInterviewsBetween(today, today));
        todayStats.put("todayRiskAlertCount", countSecurityAlertsBetween(today, today));
        todayStats.put("sevenDayActiveUserCount", countActiveUsersBetween(sevenDaysAgo, today));
        return todayStats;
    }

    private Map<String, Object> buildTrend() {
        Map<String, Object> trend = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<Long> registerTrend = new ArrayList<>();
        List<Long> practiceTrend = new ArrayList<>();
        List<Long> commentTrend = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate startDate = today.minusDays(6);
        Map<LocalDate, Long> registerTrendMap = listDateCountMapForUser(startDate, today);
        Map<LocalDate, Long> practiceTrendMap = listDateCountMapForHistory(startDate, today);
        Map<LocalDate, Long> commentTrendMap = listDateCountMapForComment(startDate, today);

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dateList.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            registerTrend.add(registerTrendMap.getOrDefault(date, 0L));
            practiceTrend.add(practiceTrendMap.getOrDefault(date, 0L));
            commentTrend.add(commentTrendMap.getOrDefault(date, 0L));
        }

        trend.put("dates", dateList);
        trend.put("registerTrend", registerTrend);
        trend.put("practiceTrend", practiceTrend);
        trend.put("commentTrend", commentTrend);
        return trend;
    }

    private Map<String, Object> buildGrowthComparison() {
        Map<String, Object> comparison = new HashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));

        long currentWeekRegisterCount = countUsersBetween(today.minusDays(6), today);
        long previousWeekRegisterCount = countUsersBetween(today.minusDays(13), today.minusDays(7));
        long currentMonthActiveUserCount = countActiveUsersBetween(today.minusDays(29), today);
        long previousMonthActiveUserCount = countActiveUsersBetween(today.minusDays(59), today.minusDays(30));
        long currentMonthRegisterCount = countUsersBetween(today.withDayOfMonth(1), today);
        LocalDate lastYearSameDay = today.minusYears(1);
        long lastYearMonthRegisterCount = countUsersBetween(lastYearSameDay.withDayOfMonth(1), lastYearSameDay);

        comparison.put("currentWeekRegisterCount", currentWeekRegisterCount);
        comparison.put("previousWeekRegisterCount", previousWeekRegisterCount);
        comparison.put("weekOverWeekRegisterRate", calculateGrowthRate(currentWeekRegisterCount, previousWeekRegisterCount));
        comparison.put("currentMonthActiveUserCount", currentMonthActiveUserCount);
        comparison.put("previousMonthActiveUserCount", previousMonthActiveUserCount);
        comparison.put("monthOverMonthActiveRate", calculateGrowthRate(currentMonthActiveUserCount, previousMonthActiveUserCount));
        comparison.put("currentMonthRegisterCount", currentMonthRegisterCount);
        comparison.put("lastYearMonthRegisterCount", lastYearMonthRegisterCount);
        comparison.put("yearOverYearRegisterRate", calculateGrowthRate(currentMonthRegisterCount, lastYearMonthRegisterCount));
        return comparison;
    }

    private Map<String, Object> buildRetentionAnalytics() {
        Map<String, Object> retentionAnalytics = new HashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        List<String> dateList = new ArrayList<>();
        List<Double> nextDayRetentionTrend = new ArrayList<>();
        for (int i = 7; i >= 1; i--) {
            LocalDate registerDate = today.minusDays(i + 1L);
            dateList.add(registerDate.format(DateTimeFormatter.ofPattern("MM-dd")));
            nextDayRetentionTrend.add(calculateRetentionRate(registerDate, registerDate.plusDays(1)));
        }
        retentionAnalytics.put("nextDayRetention", calculateRetentionRate(today.minusDays(2), today.minusDays(1)));
        retentionAnalytics.put("threeDayRetention", calculateRetentionRate(today.minusDays(4), today.minusDays(1)));
        retentionAnalytics.put("sevenDayRetention", calculateRetentionRate(today.minusDays(8), today.minusDays(1)));
        retentionAnalytics.put("dates", dateList);
        retentionAnalytics.put("nextDayRetentionTrend", nextDayRetentionTrend);
        return retentionAnalytics;
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

    private List<Map<String, Object>> buildQuestionHealth(List<Question> questionList, Map<Long, Long> practiceCountMap) {
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

    private Map<String, Object> buildSearchAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        long totalSearchCount = questionSearchLogService.count();

        QueryWrapper<QuestionSearchLog> zeroResultWrapper = new QueryWrapper<>();
        zeroResultWrapper.eq("hasNoResult", 1);
        long zeroResultSearchCount = questionSearchLogService.count(zeroResultWrapper);

        analytics.put("totalSearchCount", totalSearchCount);
        analytics.put("todaySearchCount", countSearchLogsBetween(today, today));
        analytics.put("distinctKeywordCount", countDistinctSearchKeyword());
        analytics.put("zeroResultSearchCount", zeroResultSearchCount);
        analytics.put("zeroResultRate", totalSearchCount == 0 ? 0D : Math.round(zeroResultSearchCount * 10000D / totalSearchCount) / 100D);
        analytics.put("topKeywords", buildTopSearchKeywords(false));
        analytics.put("zeroResultKeywords", buildTopSearchKeywords(true));
        analytics.put("trend", buildSearchTrend());
        return analytics;
    }

    private Map<String, Object> buildRecommendationAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        QueryWrapper<QuestionRecommendLog> exposureWrapper = new QueryWrapper<>();
        exposureWrapper.eq("action", "exposure");
        long totalExposureCount = questionRecommendLogService.count(exposureWrapper);

        QueryWrapper<QuestionRecommendLog> clickWrapper = new QueryWrapper<>();
        clickWrapper.eq("action", "click");
        long totalClickCount = questionRecommendLogService.count(clickWrapper);
        long totalPracticeCount = countRecommendationLogByAction("practice");
        long totalFavourCount = countRecommendationLogByAction("favour");
        long totalMasteredCount = countRecommendationLogByAction("mastered");

        analytics.put("totalExposureCount", totalExposureCount);
        analytics.put("totalClickCount", totalClickCount);
        analytics.put("totalPracticeCount", totalPracticeCount);
        analytics.put("totalFavourCount", totalFavourCount);
        analytics.put("totalMasteredCount", totalMasteredCount);
        analytics.put("clickThroughRate", totalExposureCount == 0 ? 0D : Math.round(totalClickCount * 10000D / totalExposureCount) / 100D);
        analytics.put("practiceConversionRate", totalClickCount == 0 ? 0D : Math.round(totalPracticeCount * 10000D / totalClickCount) / 100D);
        analytics.put("favourConversionRate", totalClickCount == 0 ? 0D : Math.round(totalFavourCount * 10000D / totalClickCount) / 100D);
        analytics.put("masteredConversionRate", totalClickCount == 0 ? 0D : Math.round(totalMasteredCount * 10000D / totalClickCount) / 100D);
        analytics.put("sourceBreakdown", buildRecommendationSourceBreakdown());
        analytics.put("trend", buildRecommendationTrend());
        return analytics;
    }

    private List<Map<String, Object>> buildRecommendationSourceBreakdown() {
        QueryWrapper<QuestionRecommendLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("source", "action", "count(*) as totalCount");
        queryWrapper.isNotNull("source");
        queryWrapper.groupBy("source", "action");
        Map<String, Map<String, Long>> sourceActionCountMap = new HashMap<>();
        questionRecommendLogService.listMaps(queryWrapper).forEach(item -> {
            String source = String.valueOf(item.get("source"));
            String action = String.valueOf(item.get("action"));
            sourceActionCountMap.computeIfAbsent(source, key -> new HashMap<>())
                    .put(action, getLongValue(item.get("totalCount")));
        });
        return sourceActionCountMap.entrySet().stream()
                .map(entry -> {
                    long exposureCount = entry.getValue().getOrDefault("exposure", 0L);
                    long clickCount = entry.getValue().getOrDefault("click", 0L);
                    long practiceCount = entry.getValue().getOrDefault("practice", 0L);
                    long favourCount = entry.getValue().getOrDefault("favour", 0L);
                    long masteredCount = entry.getValue().getOrDefault("mastered", 0L);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("source", entry.getKey());
                    item.put("exposureCount", exposureCount);
                    item.put("clickCount", clickCount);
                    item.put("practiceCount", practiceCount);
                    item.put("favourCount", favourCount);
                    item.put("masteredCount", masteredCount);
                    item.put("clickThroughRate", exposureCount == 0 ? 0D : Math.round(clickCount * 10000D / exposureCount) / 100D);
                    item.put("practiceConversionRate", clickCount == 0 ? 0D : Math.round(practiceCount * 10000D / clickCount) / 100D);
                    item.put("favourConversionRate", clickCount == 0 ? 0D : Math.round(favourCount * 10000D / clickCount) / 100D);
                    item.put("masteredConversionRate", clickCount == 0 ? 0D : Math.round(masteredCount * 10000D / clickCount) / 100D);
                    return item;
                })
                .sorted(Comparator.comparingLong((Map<String, Object> item) -> (Long) item.get("exposureCount")).reversed())
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildRecommendationTrend() {
        Map<String, Object> trend = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<Long> exposureTrend = new ArrayList<>();
        List<Long> clickTrend = new ArrayList<>();
        List<Long> practiceTrend = new ArrayList<>();
        List<Long> masteredTrend = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate startDate = today.minusDays(6);
        Map<LocalDate, Long> exposureTrendMap = listDateCountMapForRecommendationLog(startDate, today, "exposure");
        Map<LocalDate, Long> clickTrendMap = listDateCountMapForRecommendationLog(startDate, today, "click");
        Map<LocalDate, Long> practiceTrendMap = listDateCountMapForRecommendationLog(startDate, today, "practice");
        Map<LocalDate, Long> masteredTrendMap = listDateCountMapForRecommendationLog(startDate, today, "mastered");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dateList.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            exposureTrend.add(exposureTrendMap.getOrDefault(date, 0L));
            clickTrend.add(clickTrendMap.getOrDefault(date, 0L));
            practiceTrend.add(practiceTrendMap.getOrDefault(date, 0L));
            masteredTrend.add(masteredTrendMap.getOrDefault(date, 0L));
        }
        trend.put("dates", dateList);
        trend.put("exposureTrend", exposureTrend);
        trend.put("clickTrend", clickTrend);
        trend.put("practiceTrend", practiceTrend);
        trend.put("masteredTrend", masteredTrend);
        return trend;
    }

    private List<Map<String, Object>> buildTopSearchKeywords(boolean onlyNoResult) {
        QueryWrapper<QuestionSearchLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(
                "LOWER(TRIM(searchText)) as keyword",
                "count(*) as count",
                "sum(case when hasNoResult = 1 then 1 else 0 end) as zeroResultCount",
                "sum(resultCount) as resultCountTotal",
                "max(createTime) as lastSearchTime"
        );
        queryWrapper.isNotNull("searchText");
        queryWrapper.apply("TRIM(searchText) <> ''");
        if (onlyNoResult) {
            queryWrapper.eq("hasNoResult", 1);
        }
        queryWrapper.groupBy("LOWER(TRIM(searchText))");
        queryWrapper.last("order by count(*) desc, max(createTime) desc limit " + (onlyNoResult ? 10 : 20));

        return questionSearchLogService.listMaps(queryWrapper).stream()
                .map(item -> {
                    long count = getLongValue(item.get("count"));
                    Map<String, Object> keywordItem = new LinkedHashMap<>();
                    keywordItem.put("keyword", item.get("keyword"));
                    keywordItem.put("count", count);
                    keywordItem.put("zeroResultCount", getLongValue(item.get("zeroResultCount")));
                    keywordItem.put("avgResultCount", count == 0 ? 0D
                            : Math.round(getLongValue(item.get("resultCountTotal")) * 100D / count) / 100D);
                    keywordItem.put("lastSearchTime", item.get("lastSearchTime"));
                    return keywordItem;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildSearchTrend() {
        Map<String, Object> trend = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<Long> searchTrend = new ArrayList<>();
        List<Long> zeroResultTrend = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate startDate = today.minusDays(6);
        Map<LocalDate, Long> searchTrendMap = listDateCountMapForSearchLog(startDate, today, false);
        Map<LocalDate, Long> zeroResultTrendMap = listDateCountMapForSearchLog(startDate, today, true);

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dateList.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            searchTrend.add(searchTrendMap.getOrDefault(date, 0L));
            zeroResultTrend.add(zeroResultTrendMap.getOrDefault(date, 0L));
        }

        trend.put("dates", dateList);
        trend.put("searchTrend", searchTrend);
        trend.put("zeroResultTrend", zeroResultTrend);
        return trend;
    }

    private Map<String, Object> buildGeoDistribution(List<User> userList, Map<Long, Long> userPracticeCountMap) {
        Map<String, Object> geoDistribution = new HashMap<>();
        Map<Long, String> userCityMap = userList.stream()
                .filter(user -> StringUtils.isNotBlank(user.getCity()))
                .collect(Collectors.toMap(User::getId, user -> normalizeCity(user.getCity()), (a, b) -> a));

        Map<String, Long> cityUserCountMap = userCityMap.values().stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.groupingBy(city -> city, Collectors.counting()));
        Map<String, Long> cityPracticeCountMap = new HashMap<>();
        for (Map.Entry<Long, Long> entry : userPracticeCountMap.entrySet()) {
            String city = userCityMap.get(entry.getKey());
            if (StringUtils.isBlank(city)) {
                continue;
            }
            cityPracticeCountMap.merge(city, entry.getValue(), Long::sum);
        }

        List<Map<String, Object>> cityList = cityUserCountMap.entrySet().stream()
                .map(entry -> {
                    String city = entry.getKey();
                    long userCount = entry.getValue();
                    long practiceCount = cityPracticeCountMap.getOrDefault(city, 0L);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("city", city);
                    item.put("userCount", userCount);
                    item.put("practiceCount", practiceCount);
                    item.put("avgPracticeCount", userCount == 0 ? 0D : Math.round(practiceCount * 100D / userCount) / 100D);
                    return item;
                })
                .sorted(Comparator
                        .comparingLong((Map<String, Object> item) -> ((Number) item.get("practiceCount")).longValue()).reversed()
                        .thenComparing(Comparator.comparingLong((Map<String, Object> item) -> ((Number) item.get("userCount")).longValue()).reversed())
                        .thenComparing(Comparator.comparingDouble((Map<String, Object> item) -> ((Number) item.get("avgPracticeCount")).doubleValue()).reversed()))
                .limit(10)
                .collect(Collectors.toList());

        long filledUserCount = cityUserCountMap.values().stream().mapToLong(Long::longValue).sum();
        geoDistribution.put("cityCount", cityUserCountMap.size());
        geoDistribution.put("filledUserCount", filledUserCount);
        geoDistribution.put("coverageRate", userList.isEmpty() ? 0D : Math.round(filledUserCount * 10000D / userList.size()) / 100D);
        geoDistribution.put("cityList", cityList);
        return geoDistribution;
    }

    private List<Map<String, Object>> buildRiskAlerts(List<SecurityAlert> securityAlertList) {
        return securityAlertList.stream()
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

    private long countHistoryBetween(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<UserQuestionHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("updateTime", toDate(startDate, true), toDate(endDate, false));
        return userQuestionHistoryService.count(queryWrapper);
    }

    private long countCommentsBetween(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<QuestionComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("createTime", toDate(startDate, true), toDate(endDate, false));
        return questionCommentService.count(queryWrapper);
    }

    private long countMockInterviewsBetween(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<MockInterview> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("createTime", toDate(startDate, true), toDate(endDate, false));
        return mockInterviewService.count(queryWrapper);
    }

    private long countSecurityAlertsBetween(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<SecurityAlert> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("createTime", toDate(startDate, true), toDate(endDate, false));
        return securityAlertService.count(queryWrapper);
    }

    private long countSearchLogsBetween(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<QuestionSearchLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("createTime", toDate(startDate, true), toDate(endDate, false));
        return questionSearchLogService.count(queryWrapper);
    }

    private int countActiveUsersBetween(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<UserQuestionHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("count(distinct userId) as totalCount");
        queryWrapper.isNotNull("userId");
        queryWrapper.between("updateTime", toDate(startDate, true), toDate(endDate, false));
        return Math.toIntExact(extractSingleCount(userQuestionHistoryService.listMaps(queryWrapper)));
    }

    private int countDistinctSearchKeyword() {
        QueryWrapper<QuestionSearchLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("count(distinct LOWER(TRIM(searchText))) as totalCount");
        queryWrapper.isNotNull("searchText");
        queryWrapper.apply("TRIM(searchText) <> ''");
        return Math.toIntExact(extractSingleCount(questionSearchLogService.listMaps(queryWrapper)));
    }

    private double calculateRetentionRate(LocalDate registerDate, LocalDate activityDate) {
        if (registerDate == null || activityDate == null || activityDate.isBefore(registerDate)) {
            return 0D;
        }
        QueryWrapper<User> registerWrapper = new QueryWrapper<>();
        registerWrapper.select("id");
        registerWrapper.between("createTime", toDate(registerDate, true), toDate(registerDate, false));
        List<User> registerUserList = userService.list(registerWrapper);
        if (registerUserList.isEmpty()) {
            return 0D;
        }
        Set<Long> registerUserIdSet = registerUserList.stream().map(User::getId).collect(Collectors.toSet());
        QueryWrapper<UserQuestionHistory> activeWrapper = new QueryWrapper<>();
        activeWrapper.select("count(distinct userId) as totalCount");
        activeWrapper.in("userId", registerUserIdSet);
        activeWrapper.between("updateTime", toDate(activityDate, true), toDate(activityDate, false));
        long retainedUserCount = extractSingleCount(userQuestionHistoryService.listMaps(activeWrapper));
        return Math.round(retainedUserCount * 10000D / registerUserIdSet.size()) / 100D;
    }

    private double calculateGrowthRate(long currentValue, long previousValue) {
        if (previousValue <= 0) {
            return currentValue > 0 ? 100D : 0D;
        }
        return Math.round((currentValue - previousValue) * 10000D / previousValue) / 100D;
    }

    private List<User> listUserCitySnapshot() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "city");
        return userService.list(queryWrapper);
    }

    private List<Question> listDashboardQuestions() {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "title", "tags", "updateTime");
        return questionService.list(queryWrapper);
    }

    private Map<Long, Long> listPracticeCountMapByQuestion() {
        QueryWrapper<UserQuestionHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("questionId", "count(*) as practiceCount");
        queryWrapper.isNotNull("questionId");
        queryWrapper.groupBy("questionId");
        return questionPracticeMapFrom(userQuestionHistoryService.listMaps(queryWrapper), "questionId");
    }

    private Map<Long, Long> listPracticeCountMapByUser() {
        QueryWrapper<UserQuestionHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("userId", "count(*) as practiceCount");
        queryWrapper.isNotNull("userId");
        queryWrapper.groupBy("userId");
        return questionPracticeMapFrom(userQuestionHistoryService.listMaps(queryWrapper), "userId");
    }

    private List<SecurityAlert> listPendingSecurityAlerts() {
        QueryWrapper<SecurityAlert> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0);
        queryWrapper.orderByDesc("createTime");
        queryWrapper.last("limit 6");
        return securityAlertService.list(queryWrapper);
    }

    private Map<Long, Long> questionPracticeMapFrom(List<Map<String, Object>> mapList, String idKey) {
        Map<Long, Long> result = new HashMap<>();
        for (Map<String, Object> item : mapList) {
            Long id = getNullableLongValue(item.get(idKey));
            if (id == null) {
                continue;
            }
            result.put(id, getLongValue(item.get("practiceCount")));
        }
        return result;
    }

    private Map<LocalDate, Long> listDateCountMapForHistory(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<UserQuestionHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(updateTime) as statDate", "count(*) as totalCount");
        queryWrapper.between("updateTime", toDate(startDate, true), toDate(endDate, false));
        queryWrapper.groupBy("DATE(updateTime)");
        return dateCountMapFrom(userQuestionHistoryService.listMaps(queryWrapper));
    }

    private Map<LocalDate, Long> listDateCountMapForUser(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(createTime) as statDate", "count(*) as totalCount");
        queryWrapper.between("createTime", toDate(startDate, true), toDate(endDate, false));
        queryWrapper.groupBy("DATE(createTime)");
        return dateCountMapFrom(userService.listMaps(queryWrapper));
    }

    private Map<LocalDate, Long> listDateCountMapForComment(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<QuestionComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(createTime) as statDate", "count(*) as totalCount");
        queryWrapper.between("createTime", toDate(startDate, true), toDate(endDate, false));
        queryWrapper.groupBy("DATE(createTime)");
        return dateCountMapFrom(questionCommentService.listMaps(queryWrapper));
    }

    private Map<LocalDate, Long> listDateCountMapForSearchLog(LocalDate startDate, LocalDate endDate, boolean onlyNoResult) {
        QueryWrapper<QuestionSearchLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(createTime) as statDate", "count(*) as totalCount");
        queryWrapper.between("createTime", toDate(startDate, true), toDate(endDate, false));
        if (onlyNoResult) {
            queryWrapper.eq("hasNoResult", 1);
        }
        queryWrapper.groupBy("DATE(createTime)");
        return dateCountMapFrom(questionSearchLogService.listMaps(queryWrapper));
    }

    private Map<LocalDate, Long> listDateCountMapForRecommendationLog(LocalDate startDate, LocalDate endDate, String action) {
        QueryWrapper<QuestionRecommendLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(createTime) as statDate", "count(*) as totalCount");
        queryWrapper.between("createTime", toDate(startDate, true), toDate(endDate, false));
        queryWrapper.eq(StringUtils.isNotBlank(action), "action", action);
        queryWrapper.groupBy("DATE(createTime)");
        return dateCountMapFrom(questionRecommendLogService.listMaps(queryWrapper));
    }

    private long countRecommendationLogByAction(String action) {
        QueryWrapper<QuestionRecommendLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("action", action);
        return questionRecommendLogService.count(queryWrapper);
    }

    private Map<LocalDate, Long> dateCountMapFrom(List<Map<String, Object>> mapList) {
        Map<LocalDate, Long> result = new HashMap<>();
        for (Map<String, Object> item : mapList) {
            Object dateValue = item.get("statDate");
            if (dateValue == null) {
                continue;
            }
            result.put(LocalDate.parse(String.valueOf(dateValue)), getLongValue(item.get("totalCount")));
        }
        return result;
    }

    private long extractSingleCount(List<Map<String, Object>> mapList) {
        if (mapList == null || mapList.isEmpty()) {
            return 0L;
        }
        Map<String, Object> firstRow = mapList.get(0);
        if (firstRow == null || firstRow.isEmpty()) {
            return 0L;
        }
        Object value = firstRow.get("totalCount");
        if (value == null) {
            value = firstRow.values().iterator().next();
        }
        return getLongValue(value);
    }

    private long getLongValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Long getNullableLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Date toDate(LocalDate localDate, boolean startOfDay) {
        LocalDateTime localDateTime = startOfDay ? localDate.atStartOfDay() : localDate.atTime(LocalTime.MAX);
        return Date.from(localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant());
    }

    private String normalizeCity(String city) {
        return CityUtils.normalizeCity(city);
    }

}
