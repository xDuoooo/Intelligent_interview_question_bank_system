package com.xduo.springbootinit.model.dto.security;

import com.xduo.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 安全告警查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SecurityAlertQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 告警类型
     */
    private String alertType;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 状态：0-待处理 1-已处理 2-已忽略
     */
    private Integer status;

    /**
     * 关键词（命中用户名、原因、详情、IP）
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}
