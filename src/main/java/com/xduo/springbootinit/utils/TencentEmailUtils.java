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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 腾讯云邮件推送工具类
 */
@Component
@Slf4j
public class TencentEmailUtils {

    private static final String[] SES_SUPPORTED_REGIONS = {"ap-guangzhou", "ap-hongkong"};

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

    private Credential credential;

    private ClientProfile clientProfile;

    private final ConcurrentMap<String, SesClient> clientCache = new ConcurrentHashMap<>();

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
            credential = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ses.tencentcloudapi.com");
            clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
        } catch (Exception e) {
            credential = null;
            clientProfile = null;
            clientCache.clear();
            if (mockEnabled) {
                log.warn("腾讯云邮件客户端初始化失败，邮件功能将处于模拟模式", e);
            } else {
                log.error("腾讯云邮件客户端初始化失败，当前环境已关闭模拟邮件，将拒绝邮件发送", e);
            }
        }
    }

    public boolean sendTextEmail(String email, String subject, String content) {
        if (credential == null || clientProfile == null) {
            if (!mockEnabled) {
                log.error("邮件发送失败：腾讯云邮件未正确配置，email={}", maskEmail(email));
                return false;
            }
            log.info("模拟发送邮件：email={}, subject={}, content={}", maskEmail(email), subject, content);
            return true;
        }
        SendEmailRequest request = buildSendEmailRequest(email, subject, content);
        TencentCloudSDKException lastException = null;
        String lastTriedRegion = null;
        try {
            for (String candidateRegion : buildCandidateRegions()) {
                lastTriedRegion = candidateRegion;
                try {
                    SendEmailResponse response = getClient(candidateRegion).SendEmail(request);
                    if (response != null && StringUtils.isNotBlank(response.getMessageId())) {
                        if (!StringUtils.equals(candidateRegion, StringUtils.trimToEmpty(region))) {
                            log.warn("腾讯云邮件发送成功：配置地域 {} 发送失败后，已自动切换到 {}，email={}, messageId={}",
                                    StringUtils.trimToEmpty(region), candidateRegion, maskEmail(email), response.getMessageId());
                        } else {
                            log.info("邮件发送成功：email={}, region={}, messageId={}",
                                    maskEmail(email), candidateRegion, response.getMessageId());
                        }
                        return true;
                    }
                    log.error("邮件发送失败：email={}, region={}, error=腾讯云未返回 messageId",
                            maskEmail(email), candidateRegion);
                    return false;
                } catch (TencentCloudSDKException e) {
                    lastException = e;
                    if (isRegionError(e) && hasMoreFallbackRegion(candidateRegion)) {
                        log.warn("腾讯云邮件发送失败：地域 {} 不可用，准备自动切换地域重试，email={}",
                                candidateRegion, maskEmail(email), e);
                        continue;
                    }
                    logTencentEmailFailure(email, candidateRegion, e);
                    return false;
                }
            }
            if (lastException != null) {
                logTencentEmailFailure(email, lastTriedRegion, lastException);
                return false;
            }
        } catch (Exception e) {
            log.error("腾讯云邮件接口调用异常", e);
            return false;
        }
        return false;
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

    private String encodeBase64Utf8(String content) {
        String safeContent = StringUtils.defaultString(content);
        return Base64.getEncoder().encodeToString(safeContent.getBytes(StandardCharsets.UTF_8));
    }

    private SendEmailRequest buildSendEmailRequest(String email, String subject, String content) {
        SendEmailRequest request = new SendEmailRequest();
        request.setFromEmailAddress(fromEmailAddress);
        request.setDestination(new String[]{StringUtils.trimToEmpty(email)});
        request.setSubject(subject);
        if (StringUtils.isNotBlank(replyToAddresses)) {
            request.setReplyToAddresses(replyToAddresses);
        }
        Simple simple = new Simple();
        simple.setText(encodeBase64Utf8(content));
        request.setSimple(simple);
        return request;
    }

    private SesClient getClient(String targetRegion) {
        String normalizedRegion = normalizeRegion(targetRegion);
        return clientCache.computeIfAbsent(normalizedRegion,
                key -> new SesClient(credential, key, clientProfile));
    }

    private String[] buildCandidateRegions() {
        Set<String> regions = new LinkedHashSet<>();
        regions.add(normalizeRegion(region));
        for (String supportedRegion : SES_SUPPORTED_REGIONS) {
            regions.add(normalizeRegion(supportedRegion));
        }
        return regions.toArray(new String[0]);
    }

    private boolean hasMoreFallbackRegion(String currentRegion) {
        String[] candidateRegions = buildCandidateRegions();
        return candidateRegions.length > 1
                && !StringUtils.equals(candidateRegions[candidateRegions.length - 1], normalizeRegion(currentRegion));
    }

    private boolean isRegionError(TencentCloudSDKException e) {
        String message = StringUtils.defaultString(e.getMessage());
        return StringUtils.containsIgnoreCase(message, "region")
                || StringUtils.contains(message, "Region参数")
                || StringUtils.contains(message, "地域");
    }

    private String normalizeRegion(String targetRegion) {
        return StringUtils.defaultIfBlank(StringUtils.trimToNull(targetRegion), "ap-guangzhou");
    }

    private void logTencentEmailFailure(String email, String targetRegion, TencentCloudSDKException e) {
        String message = StringUtils.defaultString(e.getMessage());
        if (StringUtils.containsIgnoreCase(message, "ses:SendEmail")) {
            log.error("腾讯云邮件发送失败：当前密钥缺少 ses:SendEmail 权限，region={}, email={}",
                    targetRegion, maskEmail(email), e);
        } else if (StringUtils.containsIgnoreCase(message, "TEXT/HTML")) {
            log.error("腾讯云邮件发送失败：邮件正文编码不符合 SES 要求，region={}, email={}",
                    targetRegion, maskEmail(email), e);
        } else if (isRegionError(e)) {
            log.error("腾讯云邮件发送失败：当前地域 {} 不可用，请优先检查 TENCENT_EMAIL_REGION 或腾讯云 SES 地域开通状态，email={}",
                    targetRegion, maskEmail(email), e);
        } else {
            log.error("腾讯云邮件接口调用异常，region={}, email={}", targetRegion, maskEmail(email), e);
        }
    }
}
