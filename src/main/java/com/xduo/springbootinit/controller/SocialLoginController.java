package com.xduo.springbootinit.controller;

import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.model.vo.LoginUserVO;
import com.xduo.springbootinit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 社交登录接口（Mock 版）
 */
@RestController
@RequestMapping("/user/login/social")
@Slf4j
@Tag(name = "SocialLoginController")
public class SocialLoginController {

    @Resource
    private UserService userService;

    /**
     * 社交登录回调（Mock）
     *
     * @param platform 平台名称：github / gitee / google
     * @param code     授权码
     */
    @GetMapping("/callback")
    @Operation(summary = "社交登录回调（Mock）")
    public BaseResponse<LoginUserVO> socialLoginCallback(
            @RequestParam String platform,
            @RequestParam String code,
            HttpServletRequest request) {
        
        log.info("Social login callback: platform={}, code={}", platform, code);
        
        // 1. 模拟根据 code 获取社交用户信息
        // 在实际开发中，这里需要根据 platform 调用对应的 Open API
        String socialId = platform + "_mock_id_" + code.hashCode();
        String nickname = platform + "用户_" + code.substring(0, Math.min(code.length(), 4));
        String avatar = "https://gw.alipayobjects.com/zos/rmsportal/KDpgvguMpGfqaHPjicRK.svg";

        // 2. 调用服务层进行登录或注册
        LoginUserVO loginUserVO = userService.userLoginBySocial(platform, socialId, nickname, avatar, request);
        
        return ResultUtils.success(loginUserVO);
    }
}
