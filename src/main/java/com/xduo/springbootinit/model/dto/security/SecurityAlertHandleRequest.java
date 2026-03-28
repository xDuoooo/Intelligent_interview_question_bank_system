package com.xduo.springbootinit.model.dto.security;

import lombok.Data;

import java.io.Serializable;

/**
 * 安全告警处理请求
 */
@Data
public class SecurityAlertHandleRequest implements Serializable {

    /**
     * 告警 id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
