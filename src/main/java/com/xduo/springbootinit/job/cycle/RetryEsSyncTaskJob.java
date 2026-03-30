package com.xduo.springbootinit.job.cycle;

import com.xduo.springbootinit.annotation.DistributedLock;
import com.xduo.springbootinit.service.EsSyncTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ES 同步补偿重试任务
 */
@Component
@ConditionalOnProperty(prefix = "app.es-sync.compensation", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class RetryEsSyncTaskJob {

    @Resource
    private EsSyncTaskService esSyncTaskService;

    @Value("${app.es-sync.compensation.batch-size:200}")
    private int batchSize;

    @DistributedLock(key = "RetryEsSyncTaskJob", leaseTime = 30000, waitTime = 10000)
    @Scheduled(fixedRateString = "${app.es-sync.compensation.fixed-rate-ms:60000}")
    public void run() {
        esSyncTaskService.retryPendingTasks(batchSize);
    }
}
