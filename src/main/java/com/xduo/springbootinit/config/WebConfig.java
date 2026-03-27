package com.xduo.springbootinit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源映射配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取本地上传目录的绝对路径
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        File uploadFolder = new File(uploadPath);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        // 将 /api/files/** 映射到本地 uploads/ 目录
        // 注意：由于 context-path 是 /api，所以这里的路径是相对于 context-path 的，或者是绝对路径
        // 在 Spring WebMvc 中，直接映射外部路径需要加上 file: 前缀
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
