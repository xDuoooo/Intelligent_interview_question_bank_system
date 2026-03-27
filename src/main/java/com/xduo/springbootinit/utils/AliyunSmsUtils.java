package com.xduo.springbootinit.utils;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 阿里云短信工具类
 */
@Component
@Slf4j
public class AliyunSmsUtils {

    @Value("${aliyun.sms.accessKeyId:}")
    private String accessKeyId;

    @Value("${aliyun.sms.accessKeySecret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.signName:}")
    private String signName;

    @Value("${aliyun.sms.templateCode:}")
    private String templateCode;

    private Client client;

    @PostConstruct
    public void init() throws Exception {
        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            log.warn("阿里云短信配置未完善，SMS 功能将处于模拟模式");
            return;
        }
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("dysmsapi.aliyuncs.com");
        client = new Client(config);
    }

    /**
     * 发送短信
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否发送成功
     */
    public boolean sendMessage(String phone, String code) {
        if (client == null) {
            log.info("模拟发送短信：phone={}, code={}", phone, code);
            return true;
        }
        try {
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");

            SendSmsResponse response = client.sendSms(sendSmsRequest);
            if ("OK".equals(response.getBody().getCode())) {
                log.info("短信发送成功：phone={}", phone);
                return true;
            } else {
                log.error("短信发送失败：phone={}, error={}", phone, response.getBody().getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("阿里云短信接口调用异常", e);
            return false;
        }
    }
}
