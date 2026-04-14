package com.xduo.springbootinit.utils;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ses.v20201002.SesClient;
import com.tencentcloudapi.ses.v20201002.models.SendEmailRequest;
import com.tencentcloudapi.ses.v20201002.models.SendEmailResponse;
import com.tencentcloudapi.ses.v20201002.models.Simple;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 腾讯云邮件推送工具类
 */
@Component
@Slf4j
public class TencentEmailUtils {

    @Value("${tencent.email.secretId:}")
    private String secretId;

    @Value("${tencent.email.secretKey:}")
    private String secretKey;

    @Value("${tencent.email.region:ap-guangzhou}")
    private String region;

    @Value("${tencent.email.fromEmailAddress:}")
    private String fromEmailAddress;

    @Value("${tencent.email.replyToAddresses:}")
    private String replyToAddresses;

    @Value("${app.email.mock-enabled:true}")
    private boolean mockEnabled;

    private SesClient client;

    @PostConstruct
    public void init() {
        if (StringUtils.isAnyBlank(secretId, secretKey, fromEmailAddress)) {
            if (mockEnabled) {
                log.warn("腾讯云邮件配置未完善，邮件功能将处于模拟模式");
            } else {
                log.error("腾讯云邮件配置未完善，当前环境已关闭模拟邮件，将拒绝邮件发送");
            }
            return;
        }
        try {
            Credential credential = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ses.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            client = new SesClient(credential, region, clientProfile);
        } catch (Exception e) {
            client = null;
            if (mockEnabled) {
                log.warn("腾讯云邮件客户端初始化失败，邮件功能将处于模拟模式", e);
            } else {
                log.error("腾讯云邮件客户端初始化失败，当前环境已关闭模拟邮件，将拒绝邮件发送", e);
            }
        }
    }

    public boolean sendTextEmail(String email, String subject, String content) {
        if (client == null) {
            if (!mockEnabled) {
                log.error("邮件发送失败：腾讯云邮件未正确配置，email={}", maskEmail(email));
                return false;
            }
            log.info("模拟发送邮件：email={}, subject={}, content={}", maskEmail(email), subject, content);
            return true;
        }
        try {
            SendEmailRequest request = new SendEmailRequest();
            request.setFromEmailAddress(fromEmailAddress);
            request.setDestination(new String[]{StringUtils.trimToEmpty(email)});
            request.setSubject(subject);
            if (StringUtils.isNotBlank(replyToAddresses)) {
                request.setReplyToAddresses(replyToAddresses);
            }
            Simple simple = new Simple();
            simple.setText(content);
            request.setSimple(simple);

            SendEmailResponse response = client.SendEmail(request);
            if (response != null && StringUtils.isNotBlank(response.getMessageId())) {
                log.info("邮件发送成功：email={}, messageId={}", maskEmail(email), response.getMessageId());
                return true;
            }
            log.error("邮件发送失败：email={}, error=腾讯云未返回 messageId", maskEmail(email));
            return false;
        } catch (TencentCloudSDKException e) {
            if (StringUtils.containsIgnoreCase(e.getMessage(), "ses:SendEmail")) {
                log.error("腾讯云邮件发送失败：当前密钥缺少 ses:SendEmail 权限，email={}", maskEmail(email), e);
            } else {
                log.error("腾讯云邮件接口调用异常", e);
            }
            return false;
        } catch (Exception e) {
            log.error("腾讯云邮件接口调用异常", e);
            return false;
        }
    }

    private String maskEmail(String email) {
        if (StringUtils.isBlank(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + parts[1];
        }
        return local.substring(0, 2) + "***@" + parts[1];
    }
}
