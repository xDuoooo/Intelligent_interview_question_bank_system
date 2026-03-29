package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.mapper.QuestionRecommendLogMapper;
import com.xduo.springbootinit.model.entity.QuestionRecommendLog;
import com.xduo.springbootinit.service.QuestionRecommendLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目推荐效果日志服务实现
 */
@Service
public class QuestionRecommendLogServiceImpl extends ServiceImpl<QuestionRecommendLogMapper, QuestionRecommendLog>
        implements QuestionRecommendLogService {

    @Override
    public void logExposure(Long userId, String source, List<Long> questionIdList) {
        if (questionIdList == null || questionIdList.isEmpty() || StringUtils.isBlank(source)) {
            return;
        }
        List<QuestionRecommendLog> logList = questionIdList.stream()
                .filter(questionId -> questionId != null && questionId > 0)
                .distinct()
                .map(questionId -> buildLog(userId, source, questionId, "exposure"))
                .toList();
        if (!logList.isEmpty()) {
            this.saveBatch(logList);
        }
    }

    @Override
    public void logClick(Long userId, String source, Long questionId) {
        if (questionId == null || questionId <= 0 || StringUtils.isBlank(source)) {
            return;
        }
        this.save(buildLog(userId, source, questionId, "click"));
    }

    @Override
    public void logActionByRecentSource(Long userId, Long questionId, String action) {
        if (userId == null || userId <= 0 || questionId == null || questionId <= 0 || StringUtils.isBlank(action)) {
            return;
        }
        QueryWrapper<QuestionRecommendLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("questionId", questionId)
                .in("action", "click", "exposure")
                .orderByDesc("createTime")
                .last("limit 1");
        QuestionRecommendLog latestSourceLog = this.getOne(queryWrapper);
        if (latestSourceLog == null || StringUtils.isBlank(latestSourceLog.getSource())) {
            return;
        }
        this.save(buildLog(userId, latestSourceLog.getSource(), questionId, action));
    }

    private QuestionRecommendLog buildLog(Long userId, String source, Long questionId, String action) {
        QuestionRecommendLog log = new QuestionRecommendLog();
        log.setUserId(userId);
        log.setSource(StringUtils.trim(source));
        log.setQuestionId(questionId);
        log.setAction(action);
        return log;
    }
}
