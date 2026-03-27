package com.xduo.springbootinit.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户绑定请求（手机/邮箱）
 */
@Data
public class UserBindRequest implements Serializable {

    /**
     * 绑定目标（手机号或邮箱）
     */
    private String target;

    /**
     * 验证码
     */
    private String code;

    private static final long serialVersionUID = 1L;
}
