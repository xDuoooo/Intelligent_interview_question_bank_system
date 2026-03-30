package com.xduo.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ES 同步补偿任务
 */
@TableName(value = "es_sync_task")
@Data
public class EsSyncTask implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 同步类型：question / post
     */
    private String syncType;

    /**
     * 业务主键
     */
    private Long entityId;

    /**
     * 操作类型：upsert / delete
     */
    private String operation;

    /**
     * 待同步到 ES 的 DTO 快照
     */
    private String payload;

    /**
     * 已重试次数
     */
    private Integer retryCount;

    /**
     * 最近一次错误信息
     */
    private String lastError;

    /**
     * 下次允许重试时间
     */
    private Date nextRetryTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
