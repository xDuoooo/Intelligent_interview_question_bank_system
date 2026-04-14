package com.xduo.springbootinit.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.config.OAuthConfig;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;
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

    private static final String GOOGLE_OAUTH_STATE_SESSION_KEY = "google_oauth_state";

    @Resource
    private OAuthConfig oauthConfig;

    @Resource
    private UserService userService;

    @Value("${app.frontend-url:}")
    private String frontendUrl;

    /**
     * 跳转到 Google 授权页
     */
    @GetMapping("")
    public void login(@RequestParam(required = false) String action,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        String state = buildOAuthState(action);
        request.getSession(true).setAttribute(GOOGLE_OAUTH_STATE_SESSION_KEY, state);
        String authorizeUrl = String.format(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code" +
                        "&scope=openid%%20profile%%20email&access_type=offline&state=%s",
                oauthConfig.getGoogle().getId(),
                URLEncoder.encode(oauthConfig.getGoogle().getRedirectUri(), "UTF-8"),
                state
        );
        log.info("Redirecting to Google authorize URL: {}, state: {}", authorizeUrl, state);
        response.sendRedirect(authorizeUrl);
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code,
                         @RequestParam(required = false) String state,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Google 授权码为空");
        }
        String action = validateOAuthState(state, request);
        
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
        String accessToken = JSONUtil.parseObj(tokenBody).getStr("access_token");
        if (StringUtils.isBlank(accessToken)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Google Access Token 失败");
        }
        log.info("Google access token exchanged successfully");

        // 2. 获取用户信息
        String userUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpResponse userResponse = HttpRequest.get(userUrl)
                .header("Authorization", "Bearer " + accessToken)
                .execute();
        
        String userBody = userResponse.body();
        Map<String, Object> userInfo = JSONUtil.parseObj(userBody);
        
        // Google 使用 'sub' 作为唯一标识
        String googleId = String.valueOf(userInfo.get("sub"));
        String userName = (String) userInfo.get("name");
        String userAvatar = (String) userInfo.get("picture");

        if (StringUtils.isBlank(googleId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Google 用户资料失败");
        }
        log.info("Google user info fetched successfully");

        String frontendBaseUrl = getFrontendBaseUrl();

        // 3. 执行静默注册/登录或绑定（基于 state 显式区分意图）
        try {
            if ("bind".equals(action) && cn.dev33.satoken.stp.StpUtil.isLogin()) {
                userService.bindGoogle(cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong(), googleId);
                // 重定向回中心页
                response.sendRedirect(frontendBaseUrl + "/user/center?msg=" + URLEncoder.encode("绑定成功", "UTF-8"));
            } else {
                userService.googleLogin(googleId, userName, userAvatar, request);
                // 登录成功重定向首页
                response.sendRedirect(frontendBaseUrl + "/");
            }
        } catch (BusinessException e) {
            log.error("Google callback error", e);
            String redirectUrl = "bind".equals(action) ? frontendBaseUrl + "/user/center" : frontendBaseUrl + "/user/login";
            response.sendRedirect(redirectUrl + "?error=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    private String buildOAuthState(String action) {
        String intent = "bind".equals(action) ? "bind" : "login";
        return intent + ":" + UUID.randomUUID();
    }

    private String validateOAuthState(String state, HttpServletRequest request) {
        Object savedState = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute(GOOGLE_OAUTH_STATE_SESSION_KEY);
        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute(GOOGLE_OAUTH_STATE_SESSION_KEY);
        }
        if (!(savedState instanceof String) || StringUtils.isBlank(state) || !StringUtils.equals(state, (String) savedState)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Google 登录状态无效或已过期，请重新发起授权");
        }
        String[] parts = StringUtils.split(state, ':');
        return parts != null && parts.length > 0 ? parts[0] : "login";
    }

    private String getFrontendBaseUrl() {
        if (StringUtils.isBlank(frontendUrl)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "前端地址未配置，请检查 app.frontend-url");
        }
        return StringUtils.removeEnd(frontendUrl, "/");
    }
}
