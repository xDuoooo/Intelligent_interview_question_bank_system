package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.model.entity.QuestionBank;
import com.xduo.springbootinit.model.entity.QuestionBankQuestion;
import com.xduo.springbootinit.model.entity.QuestionFavour;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.entity.UserQuestionHistory;
import com.xduo.springbootinit.model.vo.GlobalLeaderboardVO;
import com.xduo.springbootinit.model.vo.LeaderboardBoardVO;
import com.xduo.springbootinit.model.vo.LeaderboardUserVO;
import com.xduo.springbootinit.model.vo.QuestionBankLeaderboardVO;
import com.xduo.springbootinit.service.LeaderboardService;
import com.xduo.springbootinit.service.QuestionBankQuestionService;
import com.xduo.springbootinit.service.QuestionBankService;
import com.xduo.springbootinit.service.QuestionFavourService;
import com.xduo.springbootinit.service.UserQuestionHistoryService;
import com.xduo.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * 榜单服务实现
 */
@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private static final int TOP_LIMIT = 10;

    @Resource
    private UserService userService;

    @Resource
    private UserQuestionHistoryService userQuestionHistoryService;

    @Resource
    private QuestionFavourService questionFavourService;

    @Resource
    private QuestionBankService questionBankService;

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Override
    public GlobalLeaderboardVO getGlobalLeaderboard(Long loginUserId) {
        List<User> userList = listRankableUsers();
        Map<Long, UserLeaderboardStat> statMap = buildUserLeaderboardStatMap(userList);

        List<LeaderboardBoardVO> boardList = new ArrayList<>();
        boardList.add(buildBoard(
                "overall",
                "综合成长榜",
                "综合刷题量、收藏偏好、活跃天数与连续学习表现，评估当前平台成长势能。",
                "综合分",
                statMap,
                UserLeaderboardStat::getOverallScore,
                stat -> stat.getOverallScore() + " 分",
                loginUserId
        ));
        boardList.add(buildBoard(
                "active",
                "活跃达人榜",
                "按累计活跃学习天数排序，适合展示持续投入型用户。",
                "活跃天数",
                statMap,
                UserLeaderboardStat::getActiveDays,
                stat -> stat.getActiveDays() + " 天",
                loginUserId
        ));
        boardList.add(buildBoard(
                "streak",
                "连续冲刺榜",
                "按连续学习天数排序，更适合答辩里强调自驱力和坚持度。",
                "连续天数",
                statMap,
                UserLeaderboardStat::getCurrentStreak,
                stat -> stat.getCurrentStreak() + " 天",
                loginUserId
        ));

        GlobalLeaderboardVO result = new GlobalLeaderboardVO();
        result.setBoardList(boardList);
        return result;
    }

    @Override
    public QuestionBankLeaderboardVO getQuestionBankLeaderboard(long questionBankId, Long loginUserId) {
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        if (questionBank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }

        List<QuestionBankQuestion> bankQuestionList = questionBankQuestionService.list(
                Wrappers.lambdaQuery(QuestionBankQuestion.class)
                        .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
        );
        Set<Long> bankQuestionIdSet = bankQuestionList.stream()
                .map(QuestionBankQuestion::getQuestionId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        QuestionBankLeaderboardVO result = new QuestionBankLeaderboardVO();
        result.setQuestionBankId(questionBank.getId());
        result.setQuestionBankTitle(questionBank.getTitle());
        result.setMetricLabel("完成题数");
        result.setDescription("按当前题库内已刷题数量排序，帮助用户快速看到该题库里的头部学习者。");

        if (bankQuestionIdSet.isEmpty()) {
            result.setRankingList(Collections.emptyList());
            return result;
        }

        List<User> userList = listRankableUsers();
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));
        List<UserQuestionHistory> historyList = userQuestionHistoryService.list();

        Map<Long, Set<Long>> userBankQuestionMap = new HashMap<>();
        Map<Long, Date> lastPracticeTimeMap = new HashMap<>();
        for (UserQuestionHistory history : historyList) {
            if (history.getUserId() == null || history.getQuestionId() == null || !bankQuestionIdSet.contains(history.getQuestionId())) {
                continue;
            }
            if (!userMap.containsKey(history.getUserId())) {
                continue;
            }
            userBankQuestionMap.computeIfAbsent(history.getUserId(), key -> new HashSet<>()).add(history.getQuestionId());
            Date updateTime = history.getUpdateTime();
            if (updateTime != null) {
                Date previous = lastPracticeTimeMap.get(history.getUserId());
                if (previous == null || updateTime.after(previous)) {
                    lastPracticeTimeMap.put(history.getUserId(), updateTime);
                }
            }
        }

        List<UserLeaderboardStat> rankingStatList = userBankQuestionMap.entrySet().stream()
                .map(entry -> {
                    User user = userMap.get(entry.getKey());
                    if (user == null) {
                        return null;
                    }
                    UserLeaderboardStat stat = new UserLeaderboardStat();
                    stat.setUser(user);
                    stat.setBankPracticeCount(entry.getValue().size());
                    stat.setLastActiveTime(lastPracticeTimeMap.get(user.getId()));
                    return stat;
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator
                        .comparingLong(UserLeaderboardStat::getBankPracticeCount).reversed()
                        .thenComparing(UserLeaderboardStat::getLastActiveTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(stat -> stat.getUser().getId()))
                .collect(Collectors.toList());

        List<LeaderboardUserVO> rankingList = toLeaderboardItems(
                rankingStatList.stream().limit(TOP_LIMIT).collect(Collectors.toList()),
                UserLeaderboardStat::getBankPracticeCount,
                stat -> stat.getBankPracticeCount() + " / " + bankQuestionIdSet.size()
        );
        result.setRankingList(rankingList);

        if (loginUserId != null) {
            for (int i = 0; i < rankingStatList.size(); i++) {
                UserLeaderboardStat stat = rankingStatList.get(i);
                if (stat.getUser().getId().equals(loginUserId)) {
                    result.setCurrentUserItem(toLeaderboardItem(stat, i + 1L, UserLeaderboardStat::getBankPracticeCount,
                            value -> stat.getBankPracticeCount() + " / " + bankQuestionIdSet.size()));
                    break;
                }
            }
        }
        return result;
    }

    private List<User> listRankableUsers() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("userRole", "ban");
        return userService.list(queryWrapper);
    }

    private Map<Long, UserLeaderboardStat> buildUserLeaderboardStatMap(List<User> userList) {
        Map<Long, UserLeaderboardStat> statMap = new HashMap<>();
        for (User user : userList) {
            UserLeaderboardStat stat = new UserLeaderboardStat();
            stat.setUser(user);
            statMap.put(user.getId(), stat);
        }

        List<UserQuestionHistory> historyList = userQuestionHistoryService.list();
        Map<Long, List<UserQuestionHistory>> userHistoryMap = historyList.stream()
                .filter(history -> history.getUserId() != null && statMap.containsKey(history.getUserId()))
                .collect(Collectors.groupingBy(UserQuestionHistory::getUserId));

        for (Map.Entry<Long, List<UserQuestionHistory>> entry : userHistoryMap.entrySet()) {
            UserLeaderboardStat stat = statMap.get(entry.getKey());
            List<UserQuestionHistory> userHistoryList = entry.getValue();
            stat.setTotalCount(userHistoryList.size());
            stat.setActiveDays(calculateActiveDays(userHistoryList));
            stat.setCurrentStreak(calculateCurrentStreak(userHistoryList));
            stat.setLastActiveTime(userHistoryList.stream()
                    .map(UserQuestionHistory::getUpdateTime)
                    .filter(java.util.Objects::nonNull)
                    .max(Date::compareTo)
                    .orElse(null));
        }

        List<QuestionFavour> favourList = questionFavourService.list();
        Map<Long, Long> favourCountMap = favourList.stream()
                .filter(favour -> favour.getUserId() != null && statMap.containsKey(favour.getUserId()))
                .collect(Collectors.groupingBy(QuestionFavour::getUserId, Collectors.counting()));

        for (Map.Entry<Long, Long> entry : favourCountMap.entrySet()) {
            UserLeaderboardStat stat = statMap.get(entry.getKey());
            if (stat != null) {
                stat.setFavourCount(entry.getValue());
            }
        }

        statMap.values().forEach(UserLeaderboardStat::calculateOverallScore);
        return statMap;
    }

    private LeaderboardBoardVO buildBoard(String key, String title, String description, String metricLabel,
                                          Map<Long, UserLeaderboardStat> statMap,
                                          ToLongFunction<UserLeaderboardStat> metricGetter,
                                          java.util.function.Function<UserLeaderboardStat, String> metricTextBuilder,
                                          Long loginUserId) {
        List<UserLeaderboardStat> sortedStatList = statMap.values().stream()
                .filter(stat -> metricGetter.applyAsLong(stat) > 0)
                .sorted(Comparator
                        .comparingLong(metricGetter).reversed()
                        .thenComparing(UserLeaderboardStat::getLastActiveTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(stat -> stat.getUser().getId()))
                .collect(Collectors.toList());

        LeaderboardBoardVO board = new LeaderboardBoardVO();
        board.setKey(key);
        board.setTitle(title);
        board.setDescription(description);
        board.setMetricLabel(metricLabel);
        board.setRankingList(toLeaderboardItems(
                sortedStatList.stream().limit(TOP_LIMIT).collect(Collectors.toList()),
                metricGetter,
                metricTextBuilder
        ));

        if (loginUserId != null) {
            for (int i = 0; i < sortedStatList.size(); i++) {
                UserLeaderboardStat stat = sortedStatList.get(i);
                if (stat.getUser().getId().equals(loginUserId)) {
                    board.setCurrentUserItem(toLeaderboardItem(stat, i + 1L, metricGetter, metricTextBuilder));
                    break;
                }
            }
        }
        return board;
    }

    private List<LeaderboardUserVO> toLeaderboardItems(List<UserLeaderboardStat> statList,
                                                       ToLongFunction<UserLeaderboardStat> metricGetter,
                                                       java.util.function.Function<UserLeaderboardStat, String> metricTextBuilder) {
        List<LeaderboardUserVO> itemList = new ArrayList<>();
        for (int i = 0; i < statList.size(); i++) {
            itemList.add(toLeaderboardItem(statList.get(i), i + 1L, metricGetter, metricTextBuilder));
        }
        return itemList;
    }

    private LeaderboardUserVO toLeaderboardItem(UserLeaderboardStat stat, Long rank,
                                                ToLongFunction<UserLeaderboardStat> metricGetter,
                                                java.util.function.Function<UserLeaderboardStat, String> metricTextBuilder) {
        LeaderboardUserVO item = new LeaderboardUserVO();
        item.setUserId(stat.getUser().getId());
        item.setUserName(stat.getUser().getUserName());
        item.setUserAvatar(stat.getUser().getUserAvatar());
        item.setUserRole(stat.getUser().getUserRole());
        item.setRank(rank);
        item.setMetricValue(metricGetter.applyAsLong(stat));
        item.setMetricText(metricTextBuilder.apply(stat));
        return item;
    }

    private long calculateActiveDays(List<UserQuestionHistory> historyList) {
        return historyList.stream()
                .map(UserQuestionHistory::getUpdateTime)
                .filter(java.util.Objects::nonNull)
                .map(this::toLocalDate)
                .collect(Collectors.toSet())
                .size();
    }

    private long calculateCurrentStreak(List<UserQuestionHistory> historyList) {
        List<LocalDate> activeDateList = historyList.stream()
                .map(UserQuestionHistory::getUpdateTime)
                .filter(java.util.Objects::nonNull)
                .map(this::toLocalDate)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        if (activeDateList.isEmpty()) {
            return 0;
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate latestActiveDate = activeDateList.get(0);
        if (latestActiveDate.isBefore(today.minusDays(1))) {
            return 0;
        }
        long streak = 1;
        LocalDate previousDate = latestActiveDate;
        for (int i = 1; i < activeDateList.size(); i++) {
            LocalDate currentDate = activeDateList.get(i);
            if (currentDate.equals(previousDate.minusDays(1))) {
                streak++;
                previousDate = currentDate;
            } else {
                break;
            }
        }
        return streak;
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
    }

    private static class UserLeaderboardStat {
        private User user;
        private long totalCount;
        private long favourCount;
        private long activeDays;
        private long currentStreak;
        private long overallScore;
        private long bankPracticeCount;
        private Date lastActiveTime;

        public void calculateOverallScore() {
            this.overallScore = totalCount * 5 + favourCount * 3 + activeDays * 4 + currentStreak * 6;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }

        public long getFavourCount() {
            return favourCount;
        }

        public void setFavourCount(long favourCount) {
            this.favourCount = favourCount;
        }

        public long getActiveDays() {
            return activeDays;
        }

        public void setActiveDays(long activeDays) {
            this.activeDays = activeDays;
        }

        public long getCurrentStreak() {
            return currentStreak;
        }

        public void setCurrentStreak(long currentStreak) {
            this.currentStreak = currentStreak;
        }

        public long getOverallScore() {
            return overallScore;
        }

        public long getBankPracticeCount() {
            return bankPracticeCount;
        }

        public void setBankPracticeCount(long bankPracticeCount) {
            this.bankPracticeCount = bankPracticeCount;
        }

        public Date getLastActiveTime() {
            return lastActiveTime;
        }

        public void setLastActiveTime(Date lastActiveTime) {
            this.lastActiveTime = lastActiveTime;
        }
    }
}
