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
import cn.hutool.json.JSONObject;

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

    private static final String GITEE_OAUTH_STATE_SESSION_KEY = "gitee_oauth_state";

    @Resource
    private OAuthConfig oauthConfig;

    @Resource
    private UserService userService;

    @Value("${app.frontend-url:}")
    private String frontendUrl;

    /**
     * 跳转到 Gitee 授权页
     */
    @GetMapping("")
    public void login(@RequestParam(required = false) String action,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        String state = buildOAuthState(action);
        request.getSession(true).setAttribute(GITEE_OAUTH_STATE_SESSION_KEY, state);
        String authorizeUrl = String.format(
                "https://gitee.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&state=%s",
                oauthConfig.getGitee().getId(),
                oauthConfig.getGitee().getRedirectUri(),
                state
        );
        log.info("Redirecting to Gitee authorize URL: {}, state: {}", authorizeUrl, state);
        response.sendRedirect(authorizeUrl);
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code,
                         @RequestParam(required = false) String state,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Gitee 授权码为空");
        }
        String action = validateOAuthState(state, request);

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
        if (!tokenResponse.isOk()) {
            log.warn("Gitee token exchange failed, status={}, body={}", tokenResponse.getStatus(), tokenResponse.body());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Gitee 授权失败，请稍后重试");
        }
        
        String tokenBody = tokenResponse.body();
        JSONObject tokenObject = JSONUtil.parseObj(tokenBody);
        String accessToken = StringUtils.trimToNull(tokenObject.getStr("access_token"));
        if (StringUtils.isBlank(accessToken)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Gitee Access Token 失败");
        }
        log.info("Gitee access token exchanged successfully");

        // 2. 获取用户信息
        String userUrl = "https://gitee.com/api/v5/user?access_token=" + accessToken;
        HttpResponse userResponse = HttpRequest.get(userUrl).execute();
        if (!userResponse.isOk()) {
            log.warn("Gitee user info request failed, status={}, body={}", userResponse.getStatus(), userResponse.body());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Gitee 用户信息失败，请稍后重试");
        }
        
        String userBody = userResponse.body();
        JSONObject userInfo = JSONUtil.parseObj(userBody);
        String giteeId = StringUtils.trimToNull(userInfo.getStr("id"));
        String userName = StringUtils.trimToNull(userInfo.getStr("name"));
        if (StringUtils.isBlank(userName)) {
            userName = StringUtils.trimToNull(userInfo.getStr("login"));
        }
        String userAvatar = StringUtils.trimToNull(userInfo.getStr("avatar_url"));

        if (StringUtils.isBlank(giteeId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Gitee 用户资料失败");
        }
        log.info("Gitee user info fetched successfully");

        String frontendBaseUrl = getFrontendBaseUrl();

        // 3. 执行静默注册/登录或绑定（基于 state 显式区分意图）
        try {
            if ("bind".equals(action) && cn.dev33.satoken.stp.StpUtil.isLogin()) {
                userService.bindGitee(cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong(), giteeId);
                // 重定向回中心页
                response.sendRedirect(frontendBaseUrl + "/user/center?msg=" + URLEncoder.encode("绑定成功", "UTF-8"));
            } else {
                userService.giteeLogin(giteeId, userName, userAvatar, request);
                // 登录成功重定向首页
                response.sendRedirect(frontendBaseUrl + "/");
            }
        } catch (BusinessException e) {
            log.error("Gitee callback error", e);
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
                : request.getSession(false).getAttribute(GITEE_OAUTH_STATE_SESSION_KEY);
        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute(GITEE_OAUTH_STATE_SESSION_KEY);
        }
        if (!(savedState instanceof String) || StringUtils.isBlank(state) || !StringUtils.equals(state, (String) savedState)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Gitee 登录状态无效或已过期，请重新发起授权");
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
