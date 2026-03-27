package com.xduo.springbootinit.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.RedisConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码接口
 */
@RestController
@RequestMapping("/captcha")
@Tag(name = "CaptchaController")
@Slf4j
public class CaptchaController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/get")
    @Operation(summary = "获取图形验证码")
    public BaseResponse<Map<String, String>> getCaptcha() {
        // 定义图形验证码的长、宽、验证码字符数、干扰线宽度
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(130, 48, 4, 4);
        String code = captcha.getCode();
        String imageBase64 = captcha.getImageBase64Data();
        
        // 生成唯一标识
        String uuid = UUID.randomUUID().toString();
        String redisKey = RedisConstant.getUserCaptchaRedisKey(uuid);
        
        // 存入 Redis，5 分钟有效
        stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);
        
        Map<String, String> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("image", imageBase64);
        return ResultUtils.success(result);
    }
}
