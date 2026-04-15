package com.xduo.springbootinit.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 阿里云号码验证服务短信工具类。
 */
@Component
@Slf4j
public class AliyunSmsVerifyUtils {

    @Value("${aliyun.sms.accessKeyId:}")
    private String accessKeyId;

    @Value("${aliyun.sms.accessKeySecret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.endpoint:dypnsapi.aliyuncs.com}")
    private String endpoint;

    @Value("${aliyun.sms.signName:}")
    private String signName;

    @Value("${aliyun.sms.templateCode:}")
    private String templateCode;

    @Value("${aliyun.sms.schemeName:}")
    private String schemeName;

    @Value("${aliyun.sms.countryCode:86}")
    private String countryCode;

    @Value("${aliyun.sms.codeLength:6}")
    private int codeLength;

    @Value("${aliyun.sms.validTimeSeconds:300}")
    private long validTimeSeconds;

    @Value("${aliyun.sms.intervalSeconds:60}")
    private long intervalSeconds;

    @Value("${aliyun.sms.codeType:1}")
    private long codeType;

    @Value("${aliyun.sms.duplicatePolicy:1}")
    private long duplicatePolicy;

    @Value("${aliyun.sms.caseAuthPolicy:1}")
    private long caseAuthPolicy;

    @Value("${aliyun.sms.codeParamName:code}")
    private String codeParamName;

    @Value("${aliyun.sms.validTimeParamName:min}")
    private String validTimeParamName;

    @Value("${aliyun.sms.codePlaceholder:##code##}")
    private String codePlaceholder;

    @Value("${app.sms.mock-enabled:true}")
    private boolean mockEnabled;

    private Client client;

    @PostConstruct
    public void init() {
        if (StringUtils.isAnyBlank(accessKeyId, accessKeySecret, signName, templateCode)) {
            if (mockEnabled) {
                log.warn("阿里云短信认证服务配置未完善，SMS 功能将处于模拟模式");
            } else {
                log.error("阿里云短信认证服务配置未完善，当前环境已关闭模拟短信，将拒绝短信发送");
            }
            return;
        }
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint(endpoint);
            client = new Client(config);
        } catch (Exception e) {
            client = null;
            if (mockEnabled) {
                log.warn("阿里云短信认证客户端初始化失败，SMS 功能将处于模拟模式", e);
            } else {
                log.error("阿里云短信认证客户端初始化失败，当前环境已关闭模拟短信，将拒绝短信发送", e);
            }
        }
    }

    public SmsSendResult sendVerifyCode(String phone) {
        String normalizedPhone = normalizePhone(phone);
        if (client == null) {
            if (!mockEnabled) {
                log.error("短信发送失败：阿里云短信认证未正确配置，phone={}", maskPhone(normalizedPhone));
                return new SmsSendResult(false, false, null, null, "阿里云短信认证未正确配置");
            }
            String mockCode = RandomUtil.randomNumbers(Math.max(codeLength, 4));
            log.info("模拟发送阿里云短信验证码：phone={}, code={}", maskPhone(normalizedPhone), mockCode);
            return new SmsSendResult(true, true, randomOutId(), mockCode, null);
        }
        try {
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(normalizedPhone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(buildTemplateParam())
                    .setCountryCode(normalizeCountryCode())
                    .setCodeLength((long) Math.max(codeLength, 4))
                    .setValidTime(Math.max(validTimeSeconds, 60))
                    .setInterval(Math.max(intervalSeconds, 1))
                    .setCodeType(codeType)
                    .setDuplicatePolicy(duplicatePolicy)
                    .setOutId(randomOutId());
            if (StringUtils.isNotBlank(schemeName)) {
                request.setSchemeName(schemeName);
            }
            SendSmsVerifyCodeResponse response =
                    client.sendSmsVerifyCodeWithOptions(request, new RuntimeOptions());
            if (response == null || response.getBody() == null) {
                log.error("短信发送失败：phone={}, error=阿里云未返回发送结果", maskPhone(normalizedPhone));
                return new SmsSendResult(false, false, null, null, "阿里云未返回发送结果");
            }
            var body = response.getBody();
            if (!Boolean.TRUE.equals(body.getSuccess()) || !"OK".equalsIgnoreCase(body.getCode())) {
                log.error("短信发送失败：phone={}, code={}, message={}", maskPhone(normalizedPhone),
                        body.getCode(), body.getMessage());
                return new SmsSendResult(false, false, null, null,
                        StringUtils.defaultIfBlank(body.getMessage(), body.getCode()));
            }
            String outId = body.getModel() == null
                    ? request.getOutId()
                    : StringUtils.defaultIfBlank(body.getModel().getOutId(), request.getOutId());
            log.info("短信发送成功：phone={}, outId={}", maskPhone(normalizedPhone), outId);
            return new SmsSendResult(true, false, outId, null, null);
        } catch (Exception e) {
            log.error("阿里云短信认证接口调用异常", e);
            return new SmsSendResult(false, false, null, null, e.getMessage());
        }
    }

    public SmsCheckResult checkVerifyCode(String phone, String code, String outId) {
        String normalizedPhone = normalizePhone(phone);
        if (StringUtils.isBlank(code)) {
            return new SmsCheckResult(false, false, "验证码不能为空");
        }
        if (client == null) {
            if (!mockEnabled) {
                log.error("短信核验失败：阿里云短信认证未正确配置，phone={}", maskPhone(normalizedPhone));
                return new SmsCheckResult(false, false, "阿里云短信认证未正确配置");
            }
            return new SmsCheckResult(true, true, null);
        }
        try {
            CheckSmsVerifyCodeRequest request = new CheckSmsVerifyCodeRequest()
                    .setPhoneNumber(normalizedPhone)
                    .setVerifyCode(StringUtils.trim(code))
                    .setCountryCode(normalizeCountryCode())
                    .setCaseAuthPolicy(caseAuthPolicy);
            if (StringUtils.isNotBlank(outId)) {
                request.setOutId(outId);
            }
            if (StringUtils.isNotBlank(schemeName)) {
                request.setSchemeName(schemeName);
            }
            CheckSmsVerifyCodeResponse response =
                    client.checkSmsVerifyCodeWithOptions(request, new RuntimeOptions());
            if (response == null || response.getBody() == null) {
                log.error("短信核验失败：phone={}, error=阿里云未返回核验结果", maskPhone(normalizedPhone));
                return new SmsCheckResult(false, false, "阿里云未返回核验结果");
            }
            var body = response.getBody();
            if (!Boolean.TRUE.equals(body.getSuccess()) || !"OK".equalsIgnoreCase(body.getCode())) {
                log.error("短信核验失败：phone={}, code={}, message={}", maskPhone(normalizedPhone),
                        body.getCode(), body.getMessage());
                return new SmsCheckResult(false, false, StringUtils.defaultIfBlank(body.getMessage(), body.getCode()));
            }
            boolean verified = body.getModel() != null
                    && "PASS".equalsIgnoreCase(body.getModel().getVerifyResult());
            return new SmsCheckResult(true, verified, verified ? null : "验证码错误或已过期");
        } catch (TeaException e) {
            if (isVerifyCodeBusinessFailure(e)) {
                log.info("阿里云短信验证码核验未通过：phone={}, statusCode={}, code={}, message={}",
                        maskPhone(normalizedPhone), e.getStatusCode(), e.getCode(), e.getMessage());
                return new SmsCheckResult(true, false, "验证码错误或已过期");
            }
            log.error("阿里云短信认证核验接口调用异常：phone={}, statusCode={}, code={}, data={}",
                    maskPhone(normalizedPhone), e.getStatusCode(), e.getCode(), e.getData(), e);
            return new SmsCheckResult(false, false, StringUtils.defaultIfBlank(e.getMessage(), e.getCode()));
        } catch (Exception e) {
            log.error("阿里云短信认证核验接口调用异常", e);
            return new SmsCheckResult(false, false, e.getMessage());
        }
    }

    public long getValidTimeSeconds() {
        return Math.max(validTimeSeconds, 60);
    }

    public boolean isMockEnabled() {
        return client == null && mockEnabled;
    }

    private String buildTemplateParam() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(StringUtils.defaultIfBlank(codeParamName, "code"), codePlaceholder);
        if (StringUtils.isNotBlank(validTimeParamName)) {
            long validMinutes = Math.max(1, (Math.max(validTimeSeconds, 60) + 59) / 60);
            params.put(validTimeParamName, String.valueOf(validMinutes));
        }
        return JSONUtil.toJsonStr(params);
    }

    private String normalizeCountryCode() {
        return StringUtils.removeStart(StringUtils.defaultIfBlank(countryCode, "86"), "+");
    }

    private String normalizePhone(String phone) {
        return StringUtils.trimToEmpty(phone);
    }

    private boolean isVerifyCodeBusinessFailure(TeaException e) {
        if (e == null) {
            return false;
        }
        String code = StringUtils.trimToEmpty(e.getCode());
        String message = StringUtils.trimToEmpty(e.getMessage());
        if (StringUtils.containsIgnoreCase(message, "验证失败")
                || StringUtils.containsIgnoreCase(message, "smscodeverifyfail")
                || StringUtils.containsIgnoreCase(message, "verify fail")
                || StringUtils.containsIgnoreCase(message, "invalid")) {
            return true;
        }
        return Objects.equals(e.getStatusCode(), 400)
                && (StringUtils.equals(code, "400") || StringUtils.isBlank(code));
    }

    private String randomOutId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String maskPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public record SmsSendResult(boolean success, boolean mock, String outId, String localCode, String message) {
    }

    public record SmsCheckResult(boolean success, boolean verified, String message) {
    }
}
