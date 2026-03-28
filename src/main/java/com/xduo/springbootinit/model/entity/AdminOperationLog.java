package com.xduo.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 管理员操作日志
 */
@TableName(value = "admin_operation_log")
@Data
public class AdminOperationLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作者 id
     */
    private Long userId;

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
     * 请求参数
     */
    private String params;

    /**
     * IP 地址
     */
    private String ip;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
