package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.dto.systemconfig.SystemConfigUpdateRequest;
import com.xduo.springbootinit.model.entity.SystemConfig;
import com.xduo.springbootinit.model.vo.SystemConfigVO;

/**
 * 系统配置服务
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 获取当前有效系统配置，不存在时会自动初始化默认配置。
     */
    SystemConfig getCurrentConfig();

    /**
     * 获取后台完整系统配置。
     */
    SystemConfigVO getSystemConfigVO();

    /**
     * 获取公开系统配置。
     */
    SystemConfigVO getPublicSystemConfigVO();

    /**
     * 更新系统配置。
     */
    boolean updateCurrentConfig(SystemConfigUpdateRequest systemConfigUpdateRequest);

    /**
     * 是否开放注册。
     */
    boolean isAllowRegister();

    /**
     * 是否要求图形验证码。
     */
    boolean isRequireCaptcha();

    /**
     * 是否开启维护模式。
     */
    boolean isMaintenanceMode();
}
