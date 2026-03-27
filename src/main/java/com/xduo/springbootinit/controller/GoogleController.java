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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Google 登录控制器
 *
 * @author xDuoooo
 */
@RestController
@RequestMapping("/user/login/google")
@Slf4j
public class GoogleController {

    @Resource
    private OAuthConfig oauthConfig;

    @Resource
    private UserService userService;

    /**
     * 跳转到 Google 授权页
     */
    @GetMapping("")
    public void login(HttpServletResponse response) throws IOException {
        String authorizeUrl = String.format(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code" +
                        "&scope=openid%%20profile%%20email&access_type=offline",
                oauthConfig.getGoogle().getId(),
                URLEncoder.encode(oauthConfig.getGoogle().getRedirectUri(), "UTF-8")
        );
        log.info("Redirecting to Google authorize URL: {}", authorizeUrl);
        response.sendRedirect(authorizeUrl);
    }

    /**
     * Google 回调接口
     */
    @GetMapping("/callback")
    public void callback(@RequestParam String code, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Google 授权码为空");
        }

        // 1. 换取 Access Token
        Map<String, Object> tokenParams = new HashMap<>();
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("client_id", oauthConfig.getGoogle().getId());
        tokenParams.put("client_secret", oauthConfig.getGoogle().getSecret());
        tokenParams.put("code", code);
        tokenParams.put("redirect_uri", oauthConfig.getGoogle().getRedirectUri());

        String tokenUrl = "https://oauth2.googleapis.com/token";
        HttpResponse tokenResponse = HttpRequest.post(tokenUrl)
                .form(tokenParams)
                .execute();
        
        String tokenBody = tokenResponse.body();
        log.info("Google access token response: {}", tokenBody);
        String accessToken = JSONUtil.parseObj(tokenBody).getStr("access_token");
        if (StringUtils.isBlank(accessToken)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Google Access Token 失败");
        }

        // 2. 获取用户信息
        String userUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpResponse userResponse = HttpRequest.get(userUrl)
                .header("Authorization", "Bearer " + accessToken)
                .execute();
        
        String userBody = userResponse.body();
        log.info("Google user info response: {}", userBody);
        Map<String, Object> userInfo = JSONUtil.parseObj(userBody);
        
        // Google 使用 'sub' 作为唯一标识
        String googleId = String.valueOf(userInfo.get("sub"));
        String userName = (String) userInfo.get("name");
        String userAvatar = (String) userInfo.get("picture");

        if (StringUtils.isBlank(googleId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Google 用户资料失败");
        }

        // 3. 执行静默注册/登录
        userService.googleLogin(googleId, userName, userAvatar);

        // 4. 重定向回前端首页
        response.sendRedirect("http://localhost:3000/");
    }
}
