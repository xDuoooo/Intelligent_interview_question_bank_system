package com.xduo.springbootinit.manager;

import cn.hutool.json.JSONUtil;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 服务管理 (通用 OpenAI 协议)
 */
@Component
@Slf4j
public class AiManager {

    @Value("${ai.api-key:empty}")
    private String apiKey;

    @Value("${ai.host:https://api.deepseek.com/v1}")
    private String host;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    @Value("${ai.speech-model:whisper-1}")
    private String speechModel;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * 发送 AI 请求
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return AI 回复
     */
    public String doChat(String systemPrompt, String userPrompt) {
        if (!hasConfiguredApiKey()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先配置 AI API Key");
        }

        // 构造请求参数
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", model);
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        requestMap.put("messages", messages);
        requestMap.put("temperature", 0.7);

        String json = JSONUtil.toJsonStr(requestMap);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(host + "/chat/completions")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = response.body() != null ? response.body().string() : "无响应";
                log.error("AI 请求失败: {}", errorMsg);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 服务调用异常");
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            return extractAssistantContent(responseBody);
        } catch (IOException e) {
            log.error("AI 通信异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 通信失败");
        }
    }

    /**
     * 调用兼容 OpenAI 协议的音频转写接口
     *
     * @param fileName    文件名
     * @param fileBytes   文件内容
     * @param contentType 文件类型
     * @param language    语言
     * @param prompt      转写提示词
     * @return 转写文本
     */
    public String transcribeAudio(String fileName, byte[] fileBytes, String contentType, String language, String prompt) {
        if (!hasConfiguredApiKey()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先配置 AI API Key");
        }
        if (fileBytes == null || fileBytes.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "音频内容不能为空");
        }
        String safeFileName = StringUtils.defaultIfBlank(fileName, "mock-interview-audio.webm");
        MediaType mediaType = MediaType.parse(StringUtils.defaultIfBlank(contentType, "application/octet-stream"));
        if (mediaType == null) {
            mediaType = MediaType.parse("application/octet-stream");
        }

        MultipartBody.Builder formBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", StringUtils.defaultIfBlank(speechModel, "whisper-1"))
                .addFormDataPart("file", safeFileName, RequestBody.create(fileBytes, mediaType));
        if (StringUtils.isNotBlank(language)) {
            formBuilder.addFormDataPart("language", language);
        }
        if (StringUtils.isNotBlank(prompt)) {
            formBuilder.addFormDataPart("prompt", prompt);
        }

        Request request = new Request.Builder()
                .url(host + "/audio/transcriptions")
                .post(formBuilder.build())
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = response.body() != null ? response.body().string() : "无响应";
                log.error("AI 音频转写失败: {}", errorMsg);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 音频转写失败");
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            return extractTranscriptionText(responseBody);
        } catch (IOException e) {
            log.error("AI 音频转写通信异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 音频转写通信失败");
        }
    }

    private boolean hasConfiguredApiKey() {
        return StringUtils.isNotBlank(apiKey)
                && !"empty".equalsIgnoreCase(apiKey)
                && !"sk-xxxx".equalsIgnoreCase(apiKey)
                && !apiKey.toLowerCase().contains("your_api_key");
    }

    private String extractAssistantContent(String responseBody) {
        try {
            Map<?, ?> responseMap = JSONUtil.toBean(responseBody, Map.class);
            Object choicesObj = responseMap.get("choices");
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果为空");
            }
            Object firstChoice = choices.get(0);
            if (!(firstChoice instanceof Map<?, ?> firstChoiceMap)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果格式错误");
            }
            Object messageObj = firstChoiceMap.get("message");
            if (!(messageObj instanceof Map<?, ?> messageMap)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果格式错误");
            }
            Object contentObj = messageMap.get("content");
            String content = contentObj == null ? null : String.valueOf(contentObj).trim();
            if (StringUtils.isBlank(content)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回内容为空");
            }
            return content;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析 AI 响应失败，responseBody={}", responseBody, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果格式错误");
        }
    }

    private String extractTranscriptionText(String responseBody) {
        try {
            Map<?, ?> responseMap = JSONUtil.toBean(responseBody, Map.class);
            String text = valueToText(responseMap.get("text"));
            if (StringUtils.isNotBlank(text)) {
                return text;
            }
            Object resultObj = responseMap.get("result");
            text = valueToText(resultObj);
            if (StringUtils.isNotBlank(text)) {
                return text;
            }
            Object dataObj = responseMap.get("data");
            if (dataObj instanceof Map<?, ?> dataMap) {
                text = valueToText(dataMap.get("text"));
                if (StringUtils.isNotBlank(text)) {
                    return text;
                }
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 转写内容为空");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析 AI 转写结果失败，responseBody={}", responseBody, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 转写结果格式错误");
        }
    }

    private String valueToText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return StringUtils.isBlank(text) ? null : text;
    }
}
