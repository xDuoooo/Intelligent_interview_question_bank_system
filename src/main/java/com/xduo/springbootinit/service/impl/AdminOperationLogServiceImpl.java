package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.mapper.AdminOperationLogMapper;
import com.xduo.springbootinit.model.entity.AdminOperationLog;
import com.xduo.springbootinit.service.AdminOperationLogService;
import org.springframework.stereotype.Service;

/**
 * 管理员操作日志服务实现
 */
@Service
public class AdminOperationLogServiceImpl extends ServiceImpl<AdminOperationLogMapper, AdminOperationLog>
        implements AdminOperationLogService {
}
