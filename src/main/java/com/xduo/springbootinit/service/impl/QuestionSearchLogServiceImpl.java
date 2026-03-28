package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.mapper.QuestionSearchLogMapper;
import com.xduo.springbootinit.model.entity.QuestionSearchLog;
import com.xduo.springbootinit.service.QuestionSearchLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 题目搜索日志服务实现
 */
@Service
public class QuestionSearchLogServiceImpl extends ServiceImpl<QuestionSearchLogMapper, QuestionSearchLog>
        implements QuestionSearchLogService {

    @Override
    public void recordSearch(Long userId, String searchText, long resultCount, String source, String ip) {
        if (StringUtils.isBlank(searchText)) {
            return;
        }
        QuestionSearchLog searchLog = new QuestionSearchLog();
        searchLog.setUserId(userId);
        searchLog.setSearchText(StringUtils.abbreviate(searchText.trim(), 128));
        searchLog.setSource(StringUtils.defaultIfBlank(source, "question"));
        searchLog.setResultCount((int) Math.max(0, resultCount));
        searchLog.setHasNoResult(resultCount > 0 ? 0 : 1);
        searchLog.setIp(StringUtils.abbreviate(StringUtils.defaultString(ip), 128));
        this.save(searchLog);
    }
}
