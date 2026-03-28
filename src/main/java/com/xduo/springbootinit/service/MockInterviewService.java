package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.dto.mockinterview.MockInterviewQueryRequest;
import com.xduo.springbootinit.model.entity.MockInterview;

/**
 * 模拟面试服务
 */
public interface MockInterviewService extends IService<MockInterview> {

    /**
     * 校验模拟面试参数
     */
    void validMockInterview(MockInterview mockInterview, boolean add);

    /**
     * 获取查询条件
     */
    QueryWrapper<MockInterview> getQueryWrapper(MockInterviewQueryRequest queryRequest);

    /**
     * 处理模拟面试事件
     */
    String handleInterviewEvent(MockInterview mockInterview, String event, String userMessage);
}
