package com.xduo.springbootinit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源映射配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.local-fallback-enabled:true}")
    private boolean localUploadFallbackEnabled;

    @Value("${app.upload.local-dir:${user.dir}/uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!localUploadFallbackEnabled) {
            return;
        }
        // 获取本地上传目录的绝对路径
        String normalizedUploadPath = uploadPath.endsWith(File.separator) ? uploadPath : uploadPath + File.separator;
        File uploadFolder = new File(normalizedUploadPath);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        // 将 /api/files/** 映射到本地 uploads/ 目录
        // 注意：由于 context-path 是 /api，所以这里的路径是相对于 context-path 的，或者是绝对路径
        // 在 Spring WebMvc 中，直接映射外部路径需要加上 file: 前缀
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + normalizedUploadPath);
    }
}
