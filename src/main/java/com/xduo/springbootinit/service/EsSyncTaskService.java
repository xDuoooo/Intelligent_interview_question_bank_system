package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.entity.EsSyncTask;

/**
 * ES 同步补偿服务
 */
public interface EsSyncTaskService extends IService<EsSyncTask> {

    /**
     * 记录或更新一条待重试的同步任务
     */
    void recordUpsertFailure(String syncType, Long entityId, String payload, Throwable throwable);

    /**
     * 记录或更新一条待重试的删除任务
     */
    void recordDeleteFailure(String syncType, Long entityId, Throwable throwable);

    /**
     * 同步成功后清理补偿任务
     */
    void clearTask(String syncType, Long entityId);

    /**
     * 重试到期的补偿任务
     */
    void retryPendingTasks(int batchSize);
}
