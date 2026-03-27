package com.xduo.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xduo.springbootinit.model.dto.notification.NotificationQueryRequest;
import com.xduo.springbootinit.model.entity.Notification;
import com.xduo.springbootinit.model.vo.NotificationVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 通知服务
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 获取通知封装
     *
     * @param notification
     * @param request
     * @return
     */
    NotificationVO getNotificationVO(Notification notification, HttpServletRequest request);

    /**
     * 分页获取通知封装
     *
     * @param notificationPage
     * @param request
     * @return
     */
    Page<NotificationVO> getNotificationVOPage(Page<Notification> notificationPage, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param notificationQueryRequest
     * @return
     */
    QueryWrapper<Notification> getQueryWrapper(NotificationQueryRequest notificationQueryRequest);

    /**
     * 发送通知（异步）
     *
     * @param userId
     * @param title
     * @param content
     * @param type
     */
    void sendNotification(Long userId, String title, String content, String type);
}
