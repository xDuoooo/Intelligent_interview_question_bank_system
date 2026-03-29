package com.xduo.springbootinit.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.model.dto.systemconfig.SystemConfigUpdateRequest;
import com.xduo.springbootinit.model.vo.SystemConfigVO;
import com.xduo.springbootinit.service.SystemConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统配置接口
 */
@RestController
@RequestMapping("/system_config")
public class SystemConfigController {

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 获取后台完整系统配置
     */
    @GetMapping("/get")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<SystemConfigVO> getSystemConfig() {
        return ResultUtils.success(systemConfigService.getSystemConfigVO());
    }

    /**
     * 获取公开系统配置
     */
    @GetMapping("/public/get")
    public BaseResponse<SystemConfigVO> getPublicSystemConfig() {
        return ResultUtils.success(systemConfigService.getPublicSystemConfigVO());
    }

    /**
     * 更新系统配置
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSystemConfig(@RequestBody SystemConfigUpdateRequest systemConfigUpdateRequest) {
        return ResultUtils.success(systemConfigService.updateCurrentConfig(systemConfigUpdateRequest));
    }
}
