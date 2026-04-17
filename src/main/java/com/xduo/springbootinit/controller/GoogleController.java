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
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import cn.hutool.json.JSONObject;

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

    @Value("${social.google.request-timeout-ms:10000}")
    private int googleRequestTimeoutMs;

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
        String frontendBaseUrl = getFrontendBaseUrl();
        String action = "login";
        try {
            if (StringUtils.isBlank(code)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Google 授权码为空");
            }
            action = validateOAuthState(state, request);

            Map<String, Object> tokenParams = new HashMap<>();
            tokenParams.put("grant_type", "authorization_code");
            tokenParams.put("client_id", oauthConfig.getGoogle().getId());
            tokenParams.put("client_secret", oauthConfig.getGoogle().getSecret());
            tokenParams.put("code", code);
            tokenParams.put("redirect_uri", oauthConfig.getGoogle().getRedirectUri());

            String accessToken = exchangeGoogleAccessToken(code, tokenParams);
            JSONObject userInfo = fetchGoogleUserInfo(accessToken);
            handleGoogleLoginOrBind(
                    action,
                    StringUtils.trimToNull(userInfo.getStr("sub")),
                    StringUtils.trimToNull(userInfo.getStr("name")),
                    StringUtils.trimToNull(userInfo.getStr("picture")),
                    request,
                    response
            );
        } catch (BusinessException e) {
            log.error("Google callback error", e);
            response.sendRedirect(buildErrorRedirect(frontendBaseUrl, action, e.getMessage()));
        } catch (Exception e) {
            log.error("Google OAuth request failed", e);
            response.sendRedirect(buildErrorRedirect(frontendBaseUrl, action, "Google 登录服务暂时不可用，请稍后重试或改用 GitHub / Gitee 登录"));
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

    private void handleGoogleLoginOrBind(String action,
                                         String googleId,
                                         String userName,
                                         String userAvatar,
                                         HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(googleId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Google 用户资料失败");
        }
        String frontendBaseUrl = getFrontendBaseUrl();
        if ("bind".equals(action) && cn.dev33.satoken.stp.StpUtil.isLogin()) {
            userService.bindGoogle(cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong(), googleId);
            response.sendRedirect(frontendBaseUrl + "/user/center?msg=" + URLEncoder.encode("绑定成功", StandardCharsets.UTF_8));
            return;
        }
        userService.googleLogin(googleId, userName, userAvatar, request);
        response.sendRedirect(frontendBaseUrl + "/");
    }

    private String buildErrorRedirect(String frontendBaseUrl, String action, String errorMessage) {
        String redirectPath = "bind".equals(action) ? "/user/center" : "/user/login";
        return frontendBaseUrl + redirectPath + "?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
    }

    private String exchangeGoogleAccessToken(String code, Map<String, Object> tokenParams) {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        HttpResponse tokenResponse = HttpRequest.post(tokenUrl)
                .timeout(googleRequestTimeoutMs)
                .form(tokenParams)
                .execute();
        if (!tokenResponse.isOk()) {
            log.warn("Google token exchange failed, status={}, body={}", tokenResponse.getStatus(), tokenResponse.body());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Google 授权失败，请稍后重试");
        }

        String tokenBody = tokenResponse.body();
        JSONObject tokenObject = JSONUtil.parseObj(tokenBody);
        String accessToken = StringUtils.trimToNull(tokenObject.getStr("access_token"));
        if (StringUtils.isBlank(accessToken)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Google Access Token 失败");
        }
        log.info("Google access token exchanged successfully");
        return accessToken;
    }

    private JSONObject fetchGoogleUserInfo(String accessToken) {
        String userUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpResponse userResponse = HttpRequest.get(userUrl)
                .timeout(googleRequestTimeoutMs)
                .header("Authorization", "Bearer " + accessToken)
                .execute();
        if (!userResponse.isOk()) {
            log.warn("Google user info request failed, status={}, body={}", userResponse.getStatus(), userResponse.body());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Google 用户信息失败，请稍后重试");
        }

        String userBody = userResponse.body();
        JSONObject userInfo = JSONUtil.parseObj(userBody);
        log.info("Google user info fetched successfully");
        return userInfo;
    }
}
