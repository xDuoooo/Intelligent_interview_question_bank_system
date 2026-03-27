package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.constant.CommonConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.mapper.NotificationMapper;
import com.xduo.springbootinit.model.dto.notification.NotificationQueryRequest;
import com.xduo.springbootinit.model.entity.Notification;
import com.xduo.springbootinit.model.vo.NotificationVO;
import com.xduo.springbootinit.service.NotificationService;
import com.xduo.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public NotificationVO getNotificationVO(Notification notification, HttpServletRequest request) {
        NotificationVO notificationVO = new NotificationVO();
        BeanUtils.copyProperties(notification, notificationVO);
        return notificationVO;
    }

    @Override
    public Page<NotificationVO> getNotificationVOPage(Page<Notification> notificationPage, HttpServletRequest request) {
        List<Notification> notificationList = notificationPage.getRecords();
        Page<NotificationVO> notificationVOPage = new Page<>(notificationPage.getCurrent(), notificationPage.getSize(), notificationPage.getTotal());
        if (notificationList.isEmpty()) {
            return notificationVOPage;
        }
        List<NotificationVO> notificationVOList = notificationList.stream().map(notification -> {
            return getNotificationVO(notification, request);
        }).collect(Collectors.toList());
        notificationVOPage.setRecords(notificationVOList);
        return notificationVOPage;
    }

    @Override
    public QueryWrapper<Notification> getQueryWrapper(NotificationQueryRequest notificationQueryRequest) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        if (notificationQueryRequest == null) {
            return queryWrapper;
        }
        Long id = notificationQueryRequest.getId();
        Long userId = notificationQueryRequest.getUserId();
        String title = notificationQueryRequest.getTitle();
        String content = notificationQueryRequest.getContent();
        String type = notificationQueryRequest.getType();
        Integer status = notificationQueryRequest.getStatus();
        String sortField = notificationQueryRequest.getSortField();
        String sortOrder = notificationQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.eq(StringUtils.isNotBlank(type), "type", type);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    @Async
    public void sendNotification(Long userId, String title, String content, String type) {
        if (userId == null || userId <= 0) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setStatus(0);
        this.save(notification);
    }
}
