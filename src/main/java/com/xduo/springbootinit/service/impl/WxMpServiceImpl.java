package com.xduo.springbootinit.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.config.WechatMpConfig;
import com.xduo.springbootinit.constant.RedisConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.model.dto.wxmp.WxMpCodeLoginRequest;
import com.xduo.springbootinit.model.vo.LoginUserVO;
import com.xduo.springbootinit.model.vo.WxMpLoginStatusVO;
import com.xduo.springbootinit.model.vo.WxMpLoginTicketVO;
import com.xduo.springbootinit.service.UserService;
import com.xduo.springbootinit.service.WxMpService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

/**
 * 微信公众号验证码登录服务
 */
@Service
@Slf4j
public class WxMpServiceImpl implements WxMpService {

    private static final String WX_MP_LOGIN_TICKET_SESSION_KEY = "wx_mp_login_ticket";
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_CODE_SENT = "code_sent";
    private static final String STATUS_USED = "used";
    private static final String STATUS_EXPIRED = "expired";

    @Resource
    private WechatMpConfig wechatMpConfig;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @Override
    public String verifyServer(String signature, String timestamp, String nonce, String echostr) {
        if (!isWechatMpConfigured()) {
            return "wechat mp login disabled";
        }
        if (!isSignatureValid(signature, timestamp, nonce)) {
            log.warn("微信公众号服务器校验失败: signature={}, timestamp={}, nonce={}", signature, timestamp, nonce);
            return "invalid";
        }
        return StringUtils.defaultString(echostr, "success");
    }

    @Override
    public String receiveMessage(String signature, String timestamp, String nonce, String requestBody) {
        if (!isWechatMpConfigured()) {
            return "success";
        }
        if (!isSignatureValid(signature, timestamp, nonce)) {
            log.warn("微信公众号消息签名校验失败");
            return "invalid";
        }
        if (StringUtils.isBlank(requestBody)) {
            return "success";
        }
        WxMpIncomingMessage message = parseIncomingMessage(requestBody);
        if (message == null) {
            return "success";
        }
        String replyContent = handleIncomingMessage(message);
        if (StringUtils.isBlank(replyContent)) {
            return "success";
        }
        return buildTextReply(message.getFromUserName(), message.getToUserName(), replyContent);
    }

    @Override
    public WxMpLoginTicketVO createLoginTicket(HttpServletRequest request) {
        ensureWechatMpEnabled();
        String ticket = generateUniqueTicket();
        long expireAt = System.currentTimeMillis() + Duration.ofSeconds(wechatMpConfig.getTicketExpireSeconds()).toMillis();
        WxMpLoginTicketState state = new WxMpLoginTicketState();
        state.setTicket(ticket);
        state.setStatus(STATUS_PENDING);
        state.setExpireAt(expireAt);
        saveTicketState(state, Duration.ofSeconds(wechatMpConfig.getTicketExpireSeconds()));
        request.getSession(true).setAttribute(WX_MP_LOGIN_TICKET_SESSION_KEY, ticket);

        WxMpLoginTicketVO ticketVO = new WxMpLoginTicketVO();
        ticketVO.setTicket(ticket);
        ticketVO.setKeyword(buildLoginKeyword(ticket));
        ticketVO.setExpireAt(expireAt);
        ticketVO.setAccountName(StringUtils.defaultIfBlank(wechatMpConfig.getAccountName(), "你的公众号"));
        ticketVO.setQrImageUrl(wechatMpConfig.getQrImageUrl());
        return ticketVO;
    }

    @Override
    public WxMpLoginStatusVO getLoginStatus(HttpServletRequest request) {
        String ticket = getCurrentTicket(request);
        if (StringUtils.isBlank(ticket)) {
            return buildStatus(STATUS_EXPIRED, false, "请先刷新页面获取登录口令", null);
        }
        WxMpLoginTicketState state = getTicketState(ticket);
        if (state == null || isExpired(state)) {
            clearCurrentTicket(request);
            return buildStatus(STATUS_EXPIRED, false, "登录口令已过期，请重新获取", null);
        }
        if (STATUS_USED.equals(state.getStatus())) {
            return buildStatus(STATUS_USED, true, "本次公众号验证码已使用，请重新获取新的口令", state.getExpireAt());
        }
        if (STATUS_CODE_SENT.equals(state.getStatus())) {
            return buildStatus(STATUS_CODE_SENT, true, "已向公众号对话发送验证码，请在网页中输入", state.getExpireAt());
        }
        return buildStatus(STATUS_PENDING, false, "请在公众号中发送页面展示的口令", state.getExpireAt());
    }

    @Override
    public LoginUserVO loginByCode(WxMpCodeLoginRequest request, HttpServletRequest httpServletRequest) {
        ensureWechatMpEnabled();
        String code = StringUtils.trimToNull(request == null ? null : request.getCode());
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入公众号验证码");
        }
        String ticket = getCurrentTicket(httpServletRequest);
        if (StringUtils.isBlank(ticket)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录口令不存在或已过期，请重新获取");
        }
        WxMpLoginTicketState state = getTicketState(ticket);
        if (state == null || isExpired(state)) {
            clearCurrentTicket(httpServletRequest);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录口令不存在或已过期，请重新获取");
        }
        if (!STATUS_CODE_SENT.equals(state.getStatus()) || StringUtils.isBlank(state.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先按页面提示向公众号发送登录口令");
        }
        if (!StringUtils.equals(state.getCode(), code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公众号验证码错误或已过期");
        }
        LoginUserVO loginUserVO = userService.userLoginByMpOpenId(state.getOpenId(), httpServletRequest);
        state.setStatus(STATUS_USED);
        state.setCode(null);
        saveTicketState(state, Duration.ofSeconds(60));
        clearCurrentTicket(httpServletRequest);
        return loginUserVO;
    }

    private String handleIncomingMessage(WxMpIncomingMessage message) {
        if ("event".equalsIgnoreCase(message.getMsgType())) {
            return buildWelcomeText();
        }
        if (!"text".equalsIgnoreCase(message.getMsgType())) {
            return buildHelpText();
        }
        String ticket = extractTicket(message.getContent());
        if (StringUtils.isBlank(ticket)) {
            return buildHelpText();
        }
        WxMpLoginTicketState state = getTicketState(ticket);
        if (state == null || isExpired(state)) {
            return "这个登录口令已经失效了，请回到网页刷新后重新获取。";
        }
        String openId = StringUtils.trimToEmpty(message.getFromUserName());
        if (StringUtils.isBlank(state.getOpenId())) {
            state.setOpenId(openId);
        } else if (!StringUtils.equals(state.getOpenId(), openId)) {
            return "这个登录口令已经被其他微信会话使用，请回到网页重新获取新的口令。";
        }
        String code = RandomUtil.randomNumbers(6);
        long expireAt = System.currentTimeMillis() + Duration.ofSeconds(wechatMpConfig.getCodeExpireSeconds()).toMillis();
        state.setCode(code);
        state.setStatus(STATUS_CODE_SENT);
        state.setExpireAt(expireAt);
        saveTicketState(state, Duration.ofSeconds(wechatMpConfig.getCodeExpireSeconds()));
        return String.format("你的智面登录验证码为：%s，%d 分钟内有效。请回到网页输入完成登录。", code,
                Math.max(1, wechatMpConfig.getCodeExpireSeconds() / 60));
    }

    private WxMpIncomingMessage parseIncomingMessage(String requestBody) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(requestBody)));
            Element root = document.getDocumentElement();
            WxMpIncomingMessage message = new WxMpIncomingMessage();
            message.setToUserName(extractTagValue(root, "ToUserName"));
            message.setFromUserName(extractTagValue(root, "FromUserName"));
            message.setMsgType(extractTagValue(root, "MsgType"));
            message.setContent(extractTagValue(root, "Content"));
            message.setEvent(extractTagValue(root, "Event"));
            return message;
        } catch (Exception e) {
            log.error("解析微信公众号回调 XML 失败", e);
            return null;
        }
    }

    private String extractTagValue(Element root, String tagName) {
        if (root == null || StringUtils.isBlank(tagName)) {
            return null;
        }
        if (root.getElementsByTagName(tagName).getLength() == 0) {
            return null;
        }
        return StringUtils.trimToNull(root.getElementsByTagName(tagName).item(0).getTextContent());
    }

    private String buildTextReply(String toUser, String fromUser, String content) {
        long currentTime = System.currentTimeMillis() / 1000;
        return "<xml>"
                + "<ToUserName><![CDATA[" + escapeCdata(toUser) + "]]></ToUserName>"
                + "<FromUserName><![CDATA[" + escapeCdata(fromUser) + "]]></FromUserName>"
                + "<CreateTime>" + currentTime + "</CreateTime>"
                + "<MsgType><![CDATA[text]]></MsgType>"
                + "<Content><![CDATA[" + escapeCdata(content) + "]]></Content>"
                + "</xml>";
    }

    private String escapeCdata(String raw) {
        return StringUtils.defaultString(raw).replace("]]>", "]]]]><![CDATA[>");
    }

    private String buildWelcomeText() {
        return "欢迎来到智面公众号登录助手。若你正在网页上登录，请把页面给你的口令发给我，例如：" + buildLoginKeyword("ABC12345");
    }

    private String buildHelpText() {
        return "请先在智面网页登录页获取口令，然后把它发给我，例如：" + buildLoginKeyword("ABC12345");
    }

    private String buildLoginKeyword(String ticket) {
        return StringUtils.defaultIfBlank(wechatMpConfig.getLoginKeyword(), "登录") + " " + ticket;
    }

    private String extractTicket(String content) {
        String normalizedContent = StringUtils.trimToEmpty(content).replaceAll("\\s+", " ");
        if (StringUtils.isBlank(normalizedContent)) {
            return null;
        }
        String keyword = StringUtils.defaultIfBlank(wechatMpConfig.getLoginKeyword(), "登录");
        if (normalizedContent.startsWith(keyword + " ")) {
            return StringUtils.trimToNull(normalizedContent.substring(keyword.length()));
        }
        if (StringUtils.equals(normalizedContent, keyword)) {
            return null;
        }
        if (normalizedContent.matches("^[A-Za-z0-9]{6,24}$")) {
            return normalizedContent;
        }
        return null;
    }

    private String generateUniqueTicket() {
        for (int i = 0; i < 5; i++) {
            String ticket = RandomUtil.randomString("ABCDEFGHJKLMNPQRSTUVWXYZ23456789", 8);
            if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(RedisConstant.getWxMpLoginTicketRedisKey(ticket)))) {
                return ticket;
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成公众号登录口令失败，请稍后重试");
    }

    private WxMpLoginTicketState getTicketState(String ticket) {
        if (StringUtils.isBlank(ticket)) {
            return null;
        }
        String redisValue = stringRedisTemplate.opsForValue().get(RedisConstant.getWxMpLoginTicketRedisKey(ticket));
        if (StringUtils.isBlank(redisValue)) {
            return null;
        }
        return JSONUtil.toBean(redisValue, WxMpLoginTicketState.class);
    }

    private void saveTicketState(WxMpLoginTicketState state, Duration ttl) {
        stringRedisTemplate.opsForValue().set(
                RedisConstant.getWxMpLoginTicketRedisKey(state.getTicket()),
                JSONUtil.toJsonStr(state),
                ttl
        );
    }

    private boolean isExpired(WxMpLoginTicketState state) {
        return state == null || state.getExpireAt() == null || state.getExpireAt() <= System.currentTimeMillis();
    }

    private boolean isWechatMpConfigured() {
        return wechatMpConfig.isEnabled() && StringUtils.isNotBlank(wechatMpConfig.getToken());
    }

    private void ensureWechatMpEnabled() {
        if (!isWechatMpConfigured()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "公众号验证码登录尚未配置完成");
        }
    }

    private boolean isSignatureValid(String signature, String timestamp, String nonce) {
        if (StringUtils.isAnyBlank(signature, timestamp, nonce, wechatMpConfig.getToken())) {
            return false;
        }
        String[] values = {wechatMpConfig.getToken(), timestamp, nonce};
        Arrays.sort(values);
        String raw = String.join("", values);
        String expected = DigestUtil.sha1Hex(raw.getBytes(StandardCharsets.UTF_8));
        return StringUtils.equalsIgnoreCase(signature, expected);
    }

    private String getCurrentTicket(HttpServletRequest request) {
        HttpSession session = request == null ? null : request.getSession(false);
        if (session == null) {
            return null;
        }
        Object ticket = session.getAttribute(WX_MP_LOGIN_TICKET_SESSION_KEY);
        return ticket == null ? null : String.valueOf(ticket);
    }

    private void clearCurrentTicket(HttpServletRequest request) {
        HttpSession session = request == null ? null : request.getSession(false);
        if (session != null) {
            session.removeAttribute(WX_MP_LOGIN_TICKET_SESSION_KEY);
        }
    }

    private WxMpLoginStatusVO buildStatus(String status, boolean codeSent, String message, Long expireAt) {
        WxMpLoginStatusVO statusVO = new WxMpLoginStatusVO();
        statusVO.setStatus(status);
        statusVO.setCodeSent(codeSent);
        statusVO.setMessage(message);
        statusVO.setExpireAt(expireAt);
        return statusVO;
    }

    @Data
    private static class WxMpIncomingMessage {
        private String toUserName;
        private String fromUserName;
        private String msgType;
        private String content;
        private String event;
    }

    @Data
    private static class WxMpLoginTicketState {
        private String ticket;
        private String openId;
        private String code;
        private String status;
        private Long expireAt;
    }
}
