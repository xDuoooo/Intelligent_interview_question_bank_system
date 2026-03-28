package com.xduo.springbootinit.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.model.dto.security.SecurityAlertHandleRequest;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.service.SecurityAlertService;
import com.xduo.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 安全告警接口
 */
@RestController
@RequestMapping("/security_alert")
public class SecurityAlertController {

    @Resource
    private SecurityAlertService securityAlertService;

    @Resource
    private UserService userService;

    @PostMapping("/admin/ignore")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> ignoreAlert(@RequestBody SecurityAlertHandleRequest handleRequest,
                                             HttpServletRequest request) {
        ThrowUtils.throwIf(handleRequest == null || handleRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        securityAlertService.ignoreAlert(handleRequest.getId(), loginUser.getId());
        return ResultUtils.success(true);
    }

    @PostMapping("/admin/ban")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> banUserByAlert(@RequestBody SecurityAlertHandleRequest handleRequest,
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(handleRequest == null || handleRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        securityAlertService.banUserByAlert(handleRequest.getId(), loginUser.getId());
        return ResultUtils.success(true);
    }
}
