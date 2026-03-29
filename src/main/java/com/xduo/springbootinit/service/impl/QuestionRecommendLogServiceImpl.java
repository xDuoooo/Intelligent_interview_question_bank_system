package com.xduo.springbootinit.service.impl;

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

    private QuestionRecommendLog buildLog(Long userId, String source, Long questionId, String action) {
        QuestionRecommendLog log = new QuestionRecommendLog();
        log.setUserId(userId);
        log.setSource(StringUtils.trim(source));
        log.setQuestionId(questionId);
        log.setAction(action);
        return log;
    }
}
