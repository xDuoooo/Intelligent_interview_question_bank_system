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
 * GitHub 登录控制器
 *
 * @author xDuoooo
 */
@RestController
@RequestMapping("/user/login/github")
@Slf4j
public class GithubController {

    private static final String GITHUB_OAUTH_STATE_SESSION_KEY = "github_oauth_state";

    @Resource
    private OAuthConfig oauthConfig;

    @Resource
    private UserService userService;

    @Value("${app.frontend-url:}")
    private String frontendUrl;

    /**
     * 跳转到 GitHub 授权页
     */
    @GetMapping("")
    public void login(@RequestParam(required = false) String action,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        String state = buildOAuthState(action);
        request.getSession(true).setAttribute(GITHUB_OAUTH_STATE_SESSION_KEY, state);
        String authorizeUrl = String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=read:user&state=%s",
                oauthConfig.getGithub().getId(),
                oauthConfig.getGithub().getRedirectUri(),
                state
        );
        log.info("Redirecting to GitHub authorize URL: {}, state: {}", authorizeUrl, state);
        response.sendRedirect(authorizeUrl);
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code,
                         @RequestParam(required = false) String state,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "GitHub 授权码为空");
        }
        String action = validateOAuthState(state, request);


        // 1. 换取 Access Token
        Map<String, Object> tokenParams = new HashMap<>();
        tokenParams.put("client_id", oauthConfig.getGithub().getId());
        tokenParams.put("client_secret", oauthConfig.getGithub().getSecret());
        tokenParams.put("code", code);
        tokenParams.put("redirect_uri", oauthConfig.getGithub().getRedirectUri());

        String tokenUrl = "https://github.com/login/oauth/access_token";
        HttpResponse tokenResponse = HttpRequest.post(tokenUrl)
                .header("Accept", "application/json")
                .form(tokenParams)
                .execute();
        if (!tokenResponse.isOk()) {
            log.warn("GitHub token exchange failed, status={}, body={}", tokenResponse.getStatus(), tokenResponse.body());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "GitHub 授权失败，请稍后重试");
        }
        
        String tokenBody = tokenResponse.body();
        JSONObject tokenObject = JSONUtil.parseObj(tokenBody);
        String accessToken = StringUtils.trimToNull(tokenObject.getStr("access_token"));
        if (StringUtils.isBlank(accessToken)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 GitHub Access Token 失败");
        }
        log.info("GitHub access token exchanged successfully");

        // 2. 获取用户信息
        String userUrl = "https://api.github.com/user";
        HttpResponse userResponse = HttpRequest.get(userUrl)
                .header("Authorization", "token " + accessToken)
                .execute();
        if (!userResponse.isOk()) {
            log.warn("GitHub user info request failed, status={}, body={}", userResponse.getStatus(), userResponse.body());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 GitHub 用户信息失败，请稍后重试");
        }
        
        String userBody = userResponse.body();
        JSONObject userInfo = JSONUtil.parseObj(userBody);
        String githubId = StringUtils.trimToNull(userInfo.getStr("id"));
        String userName = StringUtils.trimToNull(userInfo.getStr("login"));
        String userAvatar = StringUtils.trimToNull(userInfo.getStr("avatar_url"));

        if (StringUtils.isBlank(githubId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 GitHub 用户资料失败");
        }
        log.info("GitHub user info fetched successfully");

        String frontendBaseUrl = getFrontendBaseUrl();

        // 3. 执行静默注册/登录或绑定（基于 state 显式区分意图）
        try {
            if ("bind".equals(action) && cn.dev33.satoken.stp.StpUtil.isLogin()) {
                userService.bindGithub(cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong(), githubId);
                // 重定向回中心页
                response.sendRedirect(frontendBaseUrl + "/user/center?msg=" + URLEncoder.encode("绑定成功", "UTF-8"));
            } else {
                userService.githubLogin(githubId, userName, userAvatar, request);
                // 登录成功重定向首页
                response.sendRedirect(frontendBaseUrl + "/");
            }
        } catch (BusinessException e) {
            log.error("GitHub callback error", e);
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
                : request.getSession(false).getAttribute(GITHUB_OAUTH_STATE_SESSION_KEY);
        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute(GITHUB_OAUTH_STATE_SESSION_KEY);
        }
        if (!(savedState instanceof String) || StringUtils.isBlank(state) || !StringUtils.equals(state, (String) savedState)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "GitHub 登录状态无效或已过期，请重新发起授权");
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
