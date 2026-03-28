package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.entity.QuestionSearchLog;

/**
 * 题目搜索日志服务
 */
public interface QuestionSearchLogService extends IService<QuestionSearchLog> {

    /**
     * 记录搜索行为
     */
    void recordSearch(Long userId, String searchText, long resultCount, String source, String ip);
}
