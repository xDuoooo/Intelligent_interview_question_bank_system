package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.mapper.SecurityAlertMapper;
import com.xduo.springbootinit.model.entity.SecurityAlert;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.service.SecurityAlertService;
import com.xduo.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 安全告警服务实现
 */
@Service
public class SecurityAlertServiceImpl extends ServiceImpl<SecurityAlertMapper, SecurityAlert>
        implements SecurityAlertService {

    @Resource
    private UserService userService;

    @Override
    public void recordAlert(Long userId, String userName, String alertType, String riskLevel, String reason, String detail, String ip) {
        SecurityAlert alert = new SecurityAlert();
        alert.setUserId(userId);
        alert.setUserName(StringUtils.defaultIfBlank(userName, "未知用户"));
        alert.setAlertType(StringUtils.defaultIfBlank(alertType, "UNKNOWN"));
        alert.setRiskLevel(StringUtils.defaultIfBlank(riskLevel, "medium"));
        alert.setReason(StringUtils.abbreviate(StringUtils.defaultString(reason), 512));
        alert.setDetail(StringUtils.defaultString(detail));
        alert.setIp(StringUtils.abbreviate(StringUtils.defaultString(ip), 128));
        alert.setStatus(0);
        this.save(alert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignoreAlert(Long alertId, Long handlerUserId) {
        SecurityAlert alert = getRequiredAlert(alertId);
        ensurePendingAlert(alert);
        alert.setStatus(2);
        alert.setHandlerUserId(handlerUserId);
        alert.setHandleAction("ignore");
        alert.setHandleTime(new Date());
        this.updateById(alert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUserByAlert(Long alertId, Long handlerUserId) {
        SecurityAlert alert = getRequiredAlert(alertId);
        ensurePendingAlert(alert);
        if (alert.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该告警未关联用户，无法直接封禁");
        }
        if (alert.getUserId().equals(handlerUserId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能通过告警封禁当前管理员账号");
        }
        User targetUser = userService.getById(alert.getUserId());
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "告警关联用户不存在");
        }
        if (UserConstant.ADMIN_ROLE.equals(targetUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "管理员账号不能通过告警直接封禁");
        }
        User updateUser = new User();
        updateUser.setId(alert.getUserId());
        updateUser.setUserRole(UserConstant.BAN_ROLE);
        boolean result = userService.updateById(updateUser);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "封禁用户失败");
        }
        alert.setStatus(1);
        alert.setHandlerUserId(handlerUserId);
        alert.setHandleAction("ban_user");
        alert.setHandleTime(new Date());
        this.updateById(alert);
    }

    private SecurityAlert getRequiredAlert(Long alertId) {
        if (alertId == null || alertId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SecurityAlert alert = this.getById(alertId);
        if (alert == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "告警不存在");
        }
        return alert;
    }

    private void ensurePendingAlert(SecurityAlert alert) {
        if (alert == null || !Integer.valueOf(0).equals(alert.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "告警已处理，请勿重复操作");
        }
    }
}
