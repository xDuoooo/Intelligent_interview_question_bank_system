package com.xduo.springbootinit.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.CommonConstant;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.model.dto.security.SecurityAlertHandleRequest;
import com.xduo.springbootinit.model.dto.security.SecurityAlertQueryRequest;
import com.xduo.springbootinit.model.entity.SecurityAlert;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.service.SecurityAlertService;
import com.xduo.springbootinit.service.UserService;
import com.xduo.springbootinit.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
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

    @PostMapping("/admin/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<SecurityAlert>> listAlertByPage(@RequestBody SecurityAlertQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();
        ThrowUtils.throwIf(current < 1 || pageSize < 1 || pageSize > 100, ErrorCode.PARAMS_ERROR, "分页参数不合法");

        QueryWrapper<SecurityAlert> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(queryRequest.getUserId() != null, "userId", queryRequest.getUserId());
        queryWrapper.like(StringUtils.isNotBlank(queryRequest.getUserName()), "userName", queryRequest.getUserName());
        queryWrapper.eq(StringUtils.isNotBlank(queryRequest.getAlertType()), "alertType", queryRequest.getAlertType());
        queryWrapper.eq(StringUtils.isNotBlank(queryRequest.getRiskLevel()), "riskLevel", queryRequest.getRiskLevel());
        queryWrapper.eq(queryRequest.getStatus() != null, "status", queryRequest.getStatus());
        if (StringUtils.isNotBlank(queryRequest.getSearchText())) {
            queryWrapper.and(qw -> qw.like("userName", queryRequest.getSearchText())
                    .or()
                    .like("reason", queryRequest.getSearchText())
                    .or()
                    .like("detail", queryRequest.getSearchText())
                    .or()
                    .like("ip", queryRequest.getSearchText()));
        }

        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        queryWrapper.orderByAsc("status");
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);
        queryWrapper.orderByDesc("createTime");

        Page<SecurityAlert> page = securityAlertService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

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
