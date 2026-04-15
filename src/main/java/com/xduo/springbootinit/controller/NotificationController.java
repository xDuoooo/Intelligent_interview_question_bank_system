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
import com.xduo.springbootinit.model.dto.notification.NotificationAdminSendRequest;
import com.xduo.springbootinit.model.dto.notification.NotificationQueryRequest;
import com.xduo.springbootinit.model.entity.Notification;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.vo.NotificationVO;
import com.xduo.springbootinit.service.NotificationService;
import com.xduo.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final String NOTIFICATION_SCOPE_SINGLE = "single";
    private static final String NOTIFICATION_SCOPE_USER = "user";
    private static final String NOTIFICATION_SCOPE_ALL = "all";

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
     * 管理员发送通知（支持单用户或广播）
     */
    @PostMapping("/admin/send")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> adminSendNotification(@RequestBody NotificationAdminSendRequest sendRequest) {
        ThrowUtils.throwIf(sendRequest == null, ErrorCode.PARAMS_ERROR);

        String scope = StringUtils.defaultIfBlank(StringUtils.trimToNull(sendRequest.getScope()), NOTIFICATION_SCOPE_SINGLE);
        String title = StringUtils.trimToNull(sendRequest.getTitle());
        String content = StringUtils.trimToNull(sendRequest.getContent());
        String type = StringUtils.defaultIfBlank(StringUtils.trimToNull(sendRequest.getType()), "system");

        ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR, "通知标题不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(content), ErrorCode.PARAMS_ERROR, "通知内容不能为空");
        ThrowUtils.throwIf(StringUtils.length(title) > 80, ErrorCode.PARAMS_ERROR, "通知标题过长");
        ThrowUtils.throwIf(StringUtils.length(content) > 1000, ErrorCode.PARAMS_ERROR, "通知内容过长");
        ThrowUtils.throwIf(StringUtils.length(type) > 40, ErrorCode.PARAMS_ERROR, "通知类型过长");

        List<Long> userIdList = resolveNotificationUserIdList(scope, sendRequest.getUserId());
        ThrowUtils.throwIf(userIdList.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "未找到可发送通知的用户");

        List<Notification> notificationList = new ArrayList<>(userIdList.size());
        userIdList.forEach(userId -> {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type);
            notification.setTargetId(sendRequest.getTargetId());
            notification.setStatus(0);
            notificationList.add(notification);
        });

        boolean result = notificationService.saveBatch(notificationList, 200);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "通知发送失败");
        return ResultUtils.success((long) notificationList.size());
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
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
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
     * 删除当前用户全部已读通知
     *
     * @param request 请求
     * @return 删除数量
     */
    @PostMapping("/delete/read")
    public BaseResponse<Long> deleteReadNotification(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.eq("status", 1);
        long count = notificationService.count(queryWrapper);
        if (count <= 0) {
            return ResultUtils.success(0L);
        }
        boolean result = notificationService.remove(queryWrapper);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "清空已读通知失败");
        return ResultUtils.success(count);
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
        if (notification == null || notification.getId() == null || notification.getId() <= 0) {
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

    private List<Long> resolveNotificationUserIdList(String scope, Long userId) {
        if (NOTIFICATION_SCOPE_SINGLE.equals(scope)) {
            ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "请填写目标用户 ID");
            User targetUser = userService.getById(userId);
            ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR, "目标用户不存在");
            return List.of(userId);
        }

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id");
        if (NOTIFICATION_SCOPE_USER.equals(scope)) {
            userQueryWrapper.eq("userRole", UserConstant.DEFAULT_ROLE);
        } else if (NOTIFICATION_SCOPE_ALL.equals(scope)) {
            userQueryWrapper.ne("userRole", UserConstant.BAN_ROLE);
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "通知发送范围不合法");
        }

        return userService.list(userQueryWrapper).stream()
                .map(User::getId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toList());
    }
}
