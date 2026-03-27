package com.xduo.springbootinit.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.config.OAuthConfig;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gitee 登录控制器
 *
 * @author xDuoooo
 */
@RestController
@RequestMapping("/user/login/gitee")
@Slf4j
public class GiteeController {

    @Resource
    private OAuthConfig oauthConfig;

    @Resource
    private UserService userService;

    /**
     * 跳转到 Gitee 授权页
     */
    @GetMapping("")
    public void login(HttpServletResponse response) throws IOException {
        String authorizeUrl = String.format(
                "https://gitee.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                oauthConfig.getGitee().getId(),
                oauthConfig.getGitee().getRedirectUri()
        );
        log.info("Redirecting to Gitee authorize URL: {}", authorizeUrl);
        response.sendRedirect(authorizeUrl);
    }

    /**
     * Gitee 回调接口
     */
    @GetMapping("/callback")
    public void callback(@RequestParam String code, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Gitee 授权码为空");
        }

        // 1. 换取 Access Token
        Map<String, Object> tokenParams = new HashMap<>();
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("client_id", oauthConfig.getGitee().getId());
        tokenParams.put("client_secret", oauthConfig.getGitee().getSecret());
        tokenParams.put("code", code);
        tokenParams.put("redirect_uri", oauthConfig.getGitee().getRedirectUri());

        String tokenUrl = "https://gitee.com/oauth/token";
        HttpResponse tokenResponse = HttpRequest.post(tokenUrl)
                .form(tokenParams)
                .execute();
        
        String tokenBody = tokenResponse.body();
        log.info("Gitee access token response: {}", tokenBody);
        String accessToken = JSONUtil.parseObj(tokenBody).getStr("access_token");
        if (StringUtils.isBlank(accessToken)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Gitee Access Token 失败");
        }

        // 2. 获取用户信息
        String userUrl = "https://gitee.com/api/v5/user?access_token=" + accessToken;
        HttpResponse userResponse = HttpRequest.get(userUrl).execute();
        
        String userBody = userResponse.body();
        log.info("Gitee user info response: {}", userBody);
        Map<String, Object> userInfo = JSONUtil.parseObj(userBody);
        String giteeId = String.valueOf(userInfo.get("id"));
        String userName = (String) userInfo.get("name");
        if (StringUtils.isBlank(userName)) {
            userName = (String) userInfo.get("login");
        }
        String userAvatar = (String) userInfo.get("avatar_url");

        if (StringUtils.isBlank(giteeId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Gitee 用户资料失败");
        }

        // 3. 执行静默注册/登录
        userService.giteeLogin(giteeId, userName, userAvatar);

        // 4. 重定向回前端首页
        response.sendRedirect("http://localhost:3000/");
    }
}
