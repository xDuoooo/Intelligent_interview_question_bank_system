package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.entity.SecurityAlert;

/**
 * 安全告警服务
 */
public interface SecurityAlertService extends IService<SecurityAlert> {

    /**
     * 记录安全告警
     */
    void recordAlert(Long userId, String userName, String alertType, String riskLevel, String reason, String detail, String ip);

    /**
     * 忽略告警
     */
    void ignoreAlert(Long alertId, Long handlerUserId);

    /**
     * 基于告警一键封禁用户
     */
    void banUserByAlert(Long alertId, Long handlerUserId);
}
