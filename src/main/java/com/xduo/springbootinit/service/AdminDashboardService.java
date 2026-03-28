package com.xduo.springbootinit.service;

import java.util.Map;

/**
 * 管理端数据驾驶舱服务
 */
public interface AdminDashboardService {

    /**
     * 获取驾驶舱总览数据
     */
    Map<String, Object> getDashboardOverview();
}
