package com.xduo.springbootinit.aop;

import cn.hutool.json.JSONUtil;
import com.xduo.springbootinit.model.entity.AdminOperationLog;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.service.AdminOperationLogService;
import com.xduo.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 管理员操作日志拦截器
 */
@Aspect
@Component
@Slf4j
public class LogInterceptor {

    @Resource
    private AdminOperationLogService adminOperationLogService;

    @Resource
    private UserService userService;

    /**
     * 拦截所有管理员权限校验的方法
     */
    @Around("@annotation(cn.dev33.satoken.annotation.SaCheckRole) && execution(* com.xduo.springbootinit.controller.*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        // 先执行业务
        Object result = joinPoint.proceed();

        try {
            // 获取请求对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return result;
            HttpServletRequest request = attributes.getRequest();

            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);
            if (loginUser == null) return result;

            // 构造日志并异步保存 (此处简便起见直接保存)
            AdminOperationLog operationLog = new AdminOperationLog();
            operationLog.setUserId(loginUser.getId());
            operationLog.setUserName(loginUser.getUserName());
            operationLog.setIp(request.getRemoteAddr());
            operationLog.setMethod(joinPoint.getSignature().toShortString());
            operationLog.setParams(JSONUtil.toJsonStr(joinPoint.getArgs()));
            operationLog.setOperation(request.getMethod() + " " + request.getRequestURI());

            adminOperationLogService.save(operationLog);
        } catch (Exception e) {
            log.error("保存管理员日志失败", e);
        }

        return result;
    }
}
