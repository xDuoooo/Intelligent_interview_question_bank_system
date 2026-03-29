package com.xduo.springbootinit.controller;

import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.model.vo.LoginUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
        log.warn("Deprecated mock social login endpoint invoked: platform={}, code={}", platform, code);
        throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Mock 社交登录入口已下线，请使用正式第三方登录流程");
    }
}
