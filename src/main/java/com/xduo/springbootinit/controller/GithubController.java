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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
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

    @Resource
    private OAuthConfig oauthConfig;

    @Resource
    private UserService userService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 跳转到 GitHub 授权页
     */
    @GetMapping("")
    public void login(@RequestParam(required = false) String action, HttpServletResponse response) throws IOException {
        String state = StringUtils.isBlank(action) ? "login" : action;
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
                         jakarta.servlet.http.HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "GitHub 授权码为空");
        }


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
        
        String tokenBody = tokenResponse.body();
        log.info("GitHub access token response: {}", tokenBody);
        String accessToken = JSONUtil.parseObj(tokenBody).getStr("access_token");
        if (StringUtils.isBlank(accessToken)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 GitHub Access Token 失败");
        }

        // 2. 获取用户信息
        String userUrl = "https://api.github.com/user";
        HttpResponse userResponse = HttpRequest.get(userUrl)
                .header("Authorization", "token " + accessToken)
                .execute();
        
        String userBody = userResponse.body();
        log.info("GitHub user info response: {}", userBody);
        Map<String, Object> userInfo = JSONUtil.parseObj(userBody);
        String githubId = String.valueOf(userInfo.get("id"));
        String userName = (String) userInfo.get("login");
        String userAvatar = (String) userInfo.get("avatar_url");

        if (StringUtils.isBlank(githubId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 GitHub 用户资料失败");
        }

        // 3. 执行静默注册/登录或绑定（基于 state 显式区分意图）
        try {
            if ("bind".equals(state) && cn.dev33.satoken.stp.StpUtil.isLogin()) {
                userService.bindGithub(cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong(), githubId);
                // 重定向回中心页
                response.sendRedirect(frontendUrl + "/user/center?msg=" + URLEncoder.encode("绑定成功", "UTF-8"));
            } else {
                userService.githubLogin(githubId, userName, userAvatar, request);
                // 登录成功重定向首页
                response.sendRedirect(frontendUrl + "/");
            }
        } catch (BusinessException e) {
            log.error("GitHub callback error", e);
            String redirectUrl = "bind".equals(state) ? frontendUrl + "/user/center" : frontendUrl + "/user/login";
            response.sendRedirect(redirectUrl + "?error=" + URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }
}
