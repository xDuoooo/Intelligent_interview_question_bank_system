package com.xduo.springbootinit.model.dto.adminoperationlog;

import com.xduo.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 管理员操作日志查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AdminOperationLogQueryRequest extends PageRequest implements Serializable {

    /**
     * 操作者名称
     */
    private String userName;

    /**
     * 操作描述
     */
    private String operation;

    /**
     * 方法名
     */
    private String method;

    /**
     * IP 地址
     */
    private String ip;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    private static final long serialVersionUID = 1L;
}
