package com.xduo.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.DeleteRequest;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.model.dto.notification.NotificationAddRequest;
import com.xduo.springbootinit.model.dto.notification.NotificationQueryRequest;
import com.xduo.springbootinit.model.entity.Notification;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.vo.NotificationVO;
import com.xduo.springbootinit.service.NotificationService;
import com.xduo.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 通知接口
 */
@RestController
@RequestMapping("/notification")
@Slf4j
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建通知（仅管理员）
     *
     * @param notificationAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addNotification(@RequestBody NotificationAddRequest notificationAddRequest, HttpServletRequest request) {
        if (notificationAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Notification notification = new Notification();
        BeanUtils.copyProperties(notificationAddRequest, notification);
        boolean result = notificationService.save(notification);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newNotificationId = notification.getId();
        return ResultUtils.success(newNotificationId);
    }

    /**
     * 删除通知
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteNotification(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Notification oldNotification = notificationService.getById(id);
        ThrowUtils.throwIf(oldNotification == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldNotification.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = notificationService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新通知（仅管理员）
     *
     * @param notification
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateNotification(@RequestBody Notification notification) {
        if (notification == null || notification.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = notificationService.updateById(notification);
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取封装
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<NotificationVO> getNotificationVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Notification notification = notificationService.getById(id);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!notification.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(notificationService.getNotificationVO(notification, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param notificationQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Notification>> listNotificationByPage(@RequestBody NotificationQueryRequest notificationQueryRequest) {
        ThrowUtils.throwIf(notificationQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = notificationQueryRequest.getCurrent();
        long size = notificationQueryRequest.getPageSize();
        ThrowUtils.throwIf(current < 1 || size < 1 || size > 50, ErrorCode.PARAMS_ERROR, "分页参数不合法");
        Page<Notification> notificationPage = notificationService.page(new Page<>(current, size),
                notificationService.getQueryWrapper(notificationQueryRequest));
        return ResultUtils.success(notificationPage);
    }

    /**
     * 分页获取当前用户通知列表
     *
     * @param notificationQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<NotificationVO>> listMyNotificationVOByPage(@RequestBody NotificationQueryRequest notificationQueryRequest,
                                                                         HttpServletRequest request) {
        if (notificationQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        notificationQueryRequest.setUserId(loginUser.getId());
        long current = notificationQueryRequest.getCurrent();
        long size = notificationQueryRequest.getPageSize();
        ThrowUtils.throwIf(current < 1 || size < 1 || size > 20, ErrorCode.PARAMS_ERROR, "分页参数不合法");
        Page<Notification> notificationPage = notificationService.page(new Page<>(current, size),
                notificationService.getQueryWrapper(notificationQueryRequest));
        return ResultUtils.success(notificationService.getNotificationVOPage(notificationPage, request));
    }

    /**
     * 全部标记为已读
     *
     * @param request
     * @return
     */
    @PostMapping("/read/all")
    public BaseResponse<Boolean> readAllNotification(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Notification notification = new Notification();
        notification.setStatus(1);
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.eq("status", 0);
        boolean result = notificationService.update(notification, queryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 标记单条通知为已读
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/read")
    public BaseResponse<Boolean> readNotification(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Notification oldNotification = notificationService.getById(id);
        ThrowUtils.throwIf(oldNotification == null, ErrorCode.NOT_FOUND_ERROR);
        if (!oldNotification.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Notification notification = new Notification();
        notification.setId(id);
        notification.setStatus(1);
        boolean result = notificationService.updateById(notification);
        return ResultUtils.success(result);
    }

    // endregion
}
