package com.xduo.springbootinit.controller;

import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.model.dto.wxmp.WxMpCodeLoginRequest;
import com.xduo.springbootinit.model.vo.LoginUserVO;
import com.xduo.springbootinit.model.vo.WxMpLoginStatusVO;
import com.xduo.springbootinit.model.vo.WxMpLoginTicketVO;
import com.xduo.springbootinit.service.WxMpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信公众号相关接口
 */
@RestController
@Tag(name = "WxMpController")
public class WxMpController {

    @Resource
    private WxMpService wxMpService;

    @GetMapping("/")
    @Operation(summary = "微信公众号服务器校验", hidden = true)
    public String check(
            @RequestParam(required = false) String signature,
            @RequestParam(required = false) String timestamp,
            @RequestParam(required = false) String nonce,
            @RequestParam(required = false) String echostr) {
        return wxMpService.verifyServer(signature, timestamp, nonce, echostr);
    }

    @PostMapping(value = "/", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "接收微信公众号消息", hidden = true)
    public String receiveMessage(
            @RequestParam(required = false) String signature,
            @RequestParam(required = false) String timestamp,
            @RequestParam(required = false) String nonce,
            @RequestBody(required = false) String requestBody) {
        return wxMpService.receiveMessage(signature, timestamp, nonce, requestBody);
    }

    @GetMapping("/setMenu")
    @Operation(summary = "保留的公众号菜单接口", hidden = true)
    public String setMenu() {
        return "个人订阅号验证码登录模式无需设置自定义菜单";
    }

    @PostMapping("/wx/mp/login/ticket")
    @Operation(summary = "创建公众号验证码登录口令")
    public BaseResponse<WxMpLoginTicketVO> createLoginTicket(HttpServletRequest request) {
        return ResultUtils.success(wxMpService.createLoginTicket(request));
    }

    @GetMapping("/wx/mp/login/status")
    @Operation(summary = "查询公众号验证码登录状态")
    public BaseResponse<WxMpLoginStatusVO> getLoginStatus(HttpServletRequest request) {
        return ResultUtils.success(wxMpService.getLoginStatus(request));
    }

    @PostMapping("/wx/mp/bind/ticket")
    @Operation(summary = "创建公众号绑定口令")
    public BaseResponse<WxMpLoginTicketVO> createBindTicket(HttpServletRequest request) {
        return ResultUtils.success(wxMpService.createBindTicket(request));
    }

    @GetMapping("/wx/mp/bind/status")
    @Operation(summary = "查询公众号绑定状态")
    public BaseResponse<WxMpLoginStatusVO> getBindStatus(HttpServletRequest request) {
        return ResultUtils.success(wxMpService.getBindStatus(request));
    }

    @PostMapping("/wx/mp/login/code")
    @Operation(summary = "使用公众号验证码登录")
    public BaseResponse<LoginUserVO> loginByCode(@RequestBody WxMpCodeLoginRequest wxMpCodeLoginRequest,
                                                 HttpServletRequest request) {
        return ResultUtils.success(wxMpService.loginByCode(wxMpCodeLoginRequest, request));
    }

    @PostMapping("/wx/mp/bind/code")
    @Operation(summary = "使用公众号验证码绑定当前账号")
    public BaseResponse<LoginUserVO> bindByCode(@RequestBody WxMpCodeLoginRequest wxMpCodeLoginRequest,
                                                HttpServletRequest request) {
        return ResultUtils.success(wxMpService.bindByCode(wxMpCodeLoginRequest, request));
    }
}
