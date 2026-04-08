package com.xduo.springbootinit.utils;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 腾讯云短信工具类
 */
@Component
@Slf4j
public class TencentSmsUtils {

    @Value("${tencent.sms.secretId:}")
    private String secretId;

    @Value("${tencent.sms.secretKey:}")
    private String secretKey;

    @Value("${tencent.sms.sdkAppId:}")
    private String sdkAppId;

    @Value("${tencent.sms.signName:}")
    private String signName;

    @Value("${tencent.sms.templateId:}")
    private String templateId;

    @Value("${tencent.sms.region:ap-guangzhou}")
    private String region;

    @Value("${tencent.sms.countryCode:+86}")
    private String countryCode;

    @Value("${app.sms.mock-enabled:true}")
    private boolean mockEnabled;

    private SmsClient client;

    @PostConstruct
    public void init() {
        if (StringUtils.isAnyBlank(secretId, secretKey, sdkAppId, signName, templateId)) {
            if (mockEnabled) {
                log.warn("腾讯云短信配置未完善，SMS 功能将处于模拟模式");
            } else {
                log.error("腾讯云短信配置未完善，当前环境已关闭模拟短信，将拒绝短信发送");
            }
            return;
        }
        try {
            Credential credential = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            client = new SmsClient(credential, region, clientProfile);
        } catch (Exception e) {
            client = null;
            if (mockEnabled) {
                log.warn("腾讯云短信客户端初始化失败，SMS 功能将处于模拟模式", e);
            } else {
                log.error("腾讯云短信客户端初始化失败，当前环境已关闭模拟短信，将拒绝短信发送", e);
            }
        }
    }

    /**
     * 发送短信
     *
     * @param phone 手机号
     * @param code 验证码
     * @return 是否发送成功
     */
    public boolean sendMessage(String phone, String code) {
        if (client == null) {
            if (!mockEnabled) {
                log.error("短信发送失败：腾讯云短信未正确配置，phone={}", maskPhone(phone));
                return false;
            }
            log.info("模拟发送短信：phone={}, code={}", maskPhone(phone), code);
            return true;
        }
        try {
            SendSmsRequest request = new SendSmsRequest();
            request.setSmsSdkAppId(sdkAppId);
            request.setSignName(signName);
            request.setTemplateId(templateId);
            request.setPhoneNumberSet(new String[]{normalizePhone(phone)});
            request.setTemplateParamSet(new String[]{code});

            SendSmsResponse response = client.SendSms(request);
            if (response == null || response.getSendStatusSet() == null || response.getSendStatusSet().length == 0) {
                log.error("短信发送失败：phone={}, error=腾讯云未返回发送结果", maskPhone(phone));
                return false;
            }
            SendStatus status = response.getSendStatusSet()[0];
            if (status != null && "Ok".equalsIgnoreCase(status.getCode())) {
                log.info("短信发送成功：phone={}", maskPhone(phone));
                return true;
            }
            log.error("短信发送失败：phone={}, error={}", maskPhone(phone),
                    status == null ? "未知错误" : status.getMessage());
            return false;
        } catch (Exception e) {
            log.error("腾讯云短信接口调用异常", e);
            return false;
        }
    }

    private String normalizePhone(String phone) {
        String normalized = StringUtils.trimToEmpty(phone);
        if (normalized.startsWith("+")) {
            return normalized;
        }
        return countryCode + normalized;
    }

    private String maskPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
