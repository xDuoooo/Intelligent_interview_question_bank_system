package com.xduo.springbootinit.job.cycle;

import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.entity.UserLearningGoal;
import com.xduo.springbootinit.service.NotificationService;
import com.xduo.springbootinit.service.UserLearningGoalService;
import com.xduo.springbootinit.service.UserQuestionHistoryService;
import com.xduo.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学习目标提醒任务
 */
@Component
@Slf4j
public class LearningGoalReminderJob {

    @Resource
    private UserLearningGoalService userLearningGoalService;

    @Resource
    private UserQuestionHistoryService userQuestionHistoryService;

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * 每天晚上 8 点检查未达标用户并发送提醒
     */
    @Scheduled(cron = "0 0 20 * * ?", zone = "Asia/Shanghai")
    public void remindUsersToReachDailyGoal() {
        List<UserLearningGoal> goalList = userLearningGoalService.listReminderEnabledGoals();
        if (goalList.isEmpty()) {
            return;
        }
        Set<Long> userIdSet = goalList.stream().map(UserLearningGoal::getUserId).collect(Collectors.toSet());
        List<User> userList = userService.listByIds(userIdSet);
        java.util.Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        for (UserLearningGoal goal : goalList) {
            if (alreadyRemindedToday(goal, today)) {
                continue;
            }
            long todayCount = userQuestionHistoryService.getTodayQuestionCount(goal.getUserId());
            int dailyTarget = goal.getDailyTarget() == null ? 0 : goal.getDailyTarget();
            if (todayCount >= dailyTarget || dailyTarget <= 0) {
                continue;
            }
            User user = userMap.get(goal.getUserId());
            if (user == null) {
                continue;
            }
            String title = "今晚刷题目标还差一点";
            String content = String.format("你今天已完成 %d/%d 道题，距离目标还差 %d 道，继续加油。",
                    todayCount, dailyTarget, dailyTarget - todayCount);
            notificationService.sendNotification(user.getId(), title, content, "learning_goal_reminder", null);
            sendReminderEmail(user, title, content);
            goal.setLastReminderTime(new Date());
            userLearningGoalService.updateById(goal);
        }
    }

    private boolean alreadyRemindedToday(UserLearningGoal goal, LocalDate today) {
        if (goal.getLastReminderTime() == null) {
            return false;
        }
        LocalDate lastReminderDate = goal.getLastReminderTime()
                .toInstant()
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toLocalDate();
        return lastReminderDate.equals(today);
    }

    private void sendReminderEmail(User user, String title, String content) {
        if (user == null || org.apache.commons.lang3.StringUtils.isBlank(user.getEmail())
                || org.apache.commons.lang3.StringUtils.isBlank(fromEmail)) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("智面平台 - 学习目标提醒");
            message.setText(title + "\n\n" + content + "\n\n现在继续刷题，保持今天的学习节奏。");
            javaMailSender.send(message);
        } catch (Exception e) {
            log.warn("发送学习目标提醒邮件失败,userId={}", user.getId(), e);
        }
    }
}
