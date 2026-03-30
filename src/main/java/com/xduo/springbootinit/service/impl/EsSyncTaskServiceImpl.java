package com.xduo.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.esdao.PostEsDao;
import com.xduo.springbootinit.esdao.QuestionEsDao;
import com.xduo.springbootinit.mapper.EsSyncTaskMapper;
import com.xduo.springbootinit.model.dto.post.PostEsDTO;
import com.xduo.springbootinit.model.dto.question.QuestionEsDTO;
import com.xduo.springbootinit.model.entity.EsSyncTask;
import com.xduo.springbootinit.service.EsSyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * ES 同步补偿服务实现
 */
@Service
@Slf4j
public class EsSyncTaskServiceImpl extends ServiceImpl<EsSyncTaskMapper, EsSyncTask> implements EsSyncTaskService {

    private static final String SYNC_TYPE_QUESTION = "question";
    private static final String SYNC_TYPE_POST = "post";
    private static final String OPERATION_UPSERT = "upsert";
    private static final String OPERATION_DELETE = "delete";

    @Resource
    private QuestionEsDao questionEsDao;

    @Resource
    private PostEsDao postEsDao;

    @Override
    public void recordUpsertFailure(String syncType, Long entityId, String payload, Throwable throwable) {
        if (StringUtils.isBlank(syncType) || entityId == null || entityId <= 0 || StringUtils.isBlank(payload)) {
            return;
        }
        saveOrUpdateTask(syncType, entityId, OPERATION_UPSERT, payload, throwable);
    }

    @Override
    public void recordDeleteFailure(String syncType, Long entityId, Throwable throwable) {
        if (StringUtils.isBlank(syncType) || entityId == null || entityId <= 0) {
            return;
        }
        saveOrUpdateTask(syncType, entityId, OPERATION_DELETE, null, throwable);
    }

    @Override
    public void clearTask(String syncType, Long entityId) {
        if (StringUtils.isBlank(syncType) || entityId == null || entityId <= 0) {
            return;
        }
        QueryWrapper<EsSyncTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("syncType", syncType).eq("entityId", entityId);
        this.remove(queryWrapper);
    }

    @Override
    public void retryPendingTasks(int batchSize) {
        int safeBatchSize = Math.max(1, batchSize);
        QueryWrapper<EsSyncTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("nextRetryTime", new Date())
                .orderByAsc("nextRetryTime")
                .last("limit " + safeBatchSize);
        List<EsSyncTask> taskList = this.list(queryWrapper);
        if (taskList == null || taskList.isEmpty()) {
            return;
        }
        for (EsSyncTask task : taskList) {
            retrySingleTask(task);
        }
    }

    private void retrySingleTask(EsSyncTask task) {
        if (task == null || task.getId() == null || StringUtils.isBlank(task.getSyncType()) || task.getEntityId() == null) {
            return;
        }
        try {
            if (OPERATION_DELETE.equals(task.getOperation())) {
                doDelete(task.getSyncType(), task.getEntityId());
            } else {
                doUpsert(task.getSyncType(), task.getPayload());
            }
            this.removeById(task.getId());
        } catch (Exception e) {
            int nextRetryCount = (task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1;
            task.setRetryCount(nextRetryCount);
            task.setLastError(buildErrorMessage(e));
            task.setNextRetryTime(new Date(System.currentTimeMillis() + computeBackoffMillis(nextRetryCount)));
            this.updateById(task);
            log.error("retry es sync task failed, taskId={}, syncType={}, entityId={}",
                    task.getId(), task.getSyncType(), task.getEntityId(), e);
        }
    }

    private void doUpsert(String syncType, String payload) {
        if (StringUtils.isBlank(payload)) {
            throw new IllegalArgumentException("payload 不能为空");
        }
        if (SYNC_TYPE_QUESTION.equals(syncType)) {
            questionEsDao.save(JSONUtil.toBean(payload, QuestionEsDTO.class));
            return;
        }
        if (SYNC_TYPE_POST.equals(syncType)) {
            postEsDao.save(JSONUtil.toBean(payload, PostEsDTO.class));
            return;
        }
        throw new IllegalArgumentException("不支持的同步类型: " + syncType);
    }

    private void doDelete(String syncType, Long entityId) {
        if (SYNC_TYPE_QUESTION.equals(syncType)) {
            questionEsDao.deleteById(entityId);
            return;
        }
        if (SYNC_TYPE_POST.equals(syncType)) {
            postEsDao.deleteById(entityId);
            return;
        }
        throw new IllegalArgumentException("不支持的同步类型: " + syncType);
    }

    private void saveOrUpdateTask(String syncType, Long entityId, String operation, String payload, Throwable throwable) {
        QueryWrapper<EsSyncTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("syncType", syncType).eq("entityId", entityId);
        EsSyncTask existingTask = this.getOne(queryWrapper);
        if (existingTask == null) {
            existingTask = new EsSyncTask();
            existingTask.setSyncType(syncType);
            existingTask.setEntityId(entityId);
            existingTask.setRetryCount(0);
        }
        existingTask.setOperation(operation);
        existingTask.setPayload(payload);
        existingTask.setLastError(buildErrorMessage(throwable));
        existingTask.setNextRetryTime(new Date());
        this.saveOrUpdate(existingTask);
    }

    private long computeBackoffMillis(int retryCount) {
        long[] steps = new long[]{60_000L, 5 * 60_000L, 15 * 60_000L, 30 * 60_000L, 60 * 60_000L};
        int index = Math.min(Math.max(retryCount - 1, 0), steps.length - 1);
        return steps[index];
    }

    private String buildErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        String message = StringUtils.defaultIfBlank(throwable.getMessage(), throwable.getClass().getSimpleName());
        return StringUtils.left(message, 1000);
    }
}
