package com.xduo.springbootinit.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.service.AdminDashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端数据驾驶舱接口
 */
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Resource
    private AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Map<String, Object>> getDashboardOverview() {
        return ResultUtils.success(adminDashboardService.getDashboardOverview());
    }
}
