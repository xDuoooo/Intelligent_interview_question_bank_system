package com.xduo.springbootinit.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.common.PageRequest;
import com.xduo.springbootinit.model.entity.AdminOperationLog;
import com.xduo.springbootinit.service.AdminOperationLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员操作日志接口
 */
@RestController
@RequestMapping("/admin/log")
@Slf4j
public class AdminOperationLogController {

    @Resource
    private AdminOperationLogService adminOperationLogService;

    /**
     * 分页查询操作日志（仅管理员可用）
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AdminOperationLog>> listAdminOperationLogByPage(@RequestBody PageRequest pageRequest) {
        if (pageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = pageRequest.getCurrent();
        long size = pageRequest.getPageSize();
        
        QueryWrapper<AdminOperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("createTime");
        
        Page<AdminOperationLog> logPage = adminOperationLogService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(logPage);
    }
}
