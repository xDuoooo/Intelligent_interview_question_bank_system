package com.xduo.springbootinit.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 自定义权限验证扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合 (目前没用)
     */
    @Override
    public List<String> getPermissionList(Object loginId, String s) {
        return new ArrayList<>();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String s) {
        // 从当前登录用户信息中获取角色
        User user = userService.getById((Serializable) loginId);
        if (user == null) {
            return new ArrayList<>();
        }
        return Collections.singletonList(user.getUserRole());
    }
}
