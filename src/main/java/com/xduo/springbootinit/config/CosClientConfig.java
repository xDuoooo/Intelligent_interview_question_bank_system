package com.xduo.springbootinit.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.endpoint.EndpointBuilder;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云对象存储客户端

 */
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

    /**
     * accessKey
     */
    private String accessKey;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    /**
     * 自定义域名
     */
    private String customizedCosHost;

    @Bean
    public COSClient cosClient() {
        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 如果配置了自定义域名，则设置 EndpointBuilder
        if (StringUtils.isNotBlank(customizedCosHost)) {
            clientConfig.setEndpointBuilder(new EndpointBuilder() {
                @Override
                public String buildGeneralApiEndpoint(String bucketName) {
                    return customizedCosHost;
                }

                @Override
                public String buildGetServiceApiEndpoint() {
                    return customizedCosHost;
                }
            });
        }
        // 生成cos客户端
        return new COSClient(cred, clientConfig);
    }
}