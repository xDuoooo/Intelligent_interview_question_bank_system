package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.entity.QuestionRecommendLog;

import java.util.List;

/**
 * 题目推荐效果日志服务
 */
public interface QuestionRecommendLogService extends IService<QuestionRecommendLog> {

    /**
     * 批量记录推荐曝光
     */
    void logExposure(Long userId, String source, List<Long> questionIdList);

    /**
     * 记录推荐点击
     */
    void logClick(Long userId, String source, Long questionId);
}
