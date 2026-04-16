package com.xduo.springbootinit.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于请求头解析用户城市
 */
@Component
@Slf4j
public class IpCityResolver {

    private static final List<String> CITY_HEADER_CANDIDATES = List.of(
            "X-Appengine-City",
            "CF-IPCity",
            "X-City",
            "X-Real-City",
            "X-Geo-City",
            "X-Tencent-Geoip-City",
            "Ali-Cdn-Real-Ip-City",
            "X-CDN-IP-City",
            "X-Location-City",
            "X-Client-City"
    );

    @Value("${app.ip-location.dev-city:}")
    private String devCity;

    @Value("${app.ip-location.lookup-enabled:true}")
    private boolean lookupEnabled;

    @Value("${app.ip-location.lookup-url-template:https://whois.pconline.com.cn/ipJson.jsp?json=true&ip={ip}}")
    private String lookupUrlTemplate;

    @Value("${app.ip-location.lookup-timeout-ms:1500}")
    private int lookupTimeoutMs;

    /**
     * 解析系统支持的城市名称
     */
    public String resolveSupportedCity(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        for (String headerName : CITY_HEADER_CANDIDATES) {
            String headerValue = request.getHeader(headerName);
            String supportedCity = CityUtils.extractSupportedCity(headerValue);
            if (StringUtils.isNotBlank(supportedCity)) {
                return supportedCity;
            }
        }
        String regionHint = String.join(" ",
                StringUtils.defaultString(request.getHeader("X-Province")),
                StringUtils.defaultString(request.getHeader("X-Region")),
                StringUtils.defaultString(request.getHeader("X-Area")),
                StringUtils.defaultString(request.getHeader("X-City-Name")));
        String supportedCity = CityUtils.extractSupportedCity(regionHint);
        if (StringUtils.isNotBlank(supportedCity)) {
            return supportedCity;
        }
        String ip = NetUtils.getIpAddress(request);
        if (isLocalOrPrivateIp(ip)) {
            return CityUtils.normalizeSupportedCity(devCity);
        }
        return resolveSupportedCityByPublicIp(ip);
    }

    private boolean isLocalOrPrivateIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return true;
        }
        String normalizedIp = ip.trim();
        if ("127.0.0.1".equals(normalizedIp)
                || "::1".equals(normalizedIp)
                || normalizedIp.startsWith("10.")
                || normalizedIp.startsWith("192.168.")
                || normalizedIp.startsWith("169.254.")
                || normalizedIp.startsWith("fd")
                || normalizedIp.startsWith("fe80")) {
            return true;
        }
        if (normalizedIp.startsWith("172.")) {
            String[] parts = normalizedIp.split("\\.");
            if (parts.length > 1) {
                try {
                    int secondSegment = Integer.parseInt(parts[1]);
                    return secondSegment >= 16 && secondSegment <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return false;
    }

    private String resolveSupportedCityByPublicIp(String ip) {
        if (!lookupEnabled || StringUtils.isBlank(ip) || StringUtils.isBlank(lookupUrlTemplate)) {
            return null;
        }
        try {
            String lookupUrl = buildLookupUrl(ip);
            HttpResponse response = HttpRequest.get(lookupUrl)
                    .timeout(Math.max(500, lookupTimeoutMs))
                    .execute();
            if (!response.isOk()) {
                log.warn("IP 城市解析失败: ip={}, status={}", ip, response.getStatus());
                return null;
            }
            JSONObject payload = parseLookupPayload(response.body());
            if (payload == null) {
                return null;
            }
            String resolvedCity = extractSupportedCityFromPayload(payload);
            if (StringUtils.isBlank(resolvedCity)) {
                log.debug("IP 城市解析未命中支持城市: ip={}, payload={}", ip, payload);
                return null;
            }
            return resolvedCity;
        } catch (Exception e) {
            log.warn("IP 城市解析异常: ip={}, message={}", ip, e.getMessage());
            return null;
        }
    }

    private String buildLookupUrl(String ip) {
        String encodedIp = java.net.URLEncoder.encode(ip, StandardCharsets.UTF_8);
        if (lookupUrlTemplate.contains("{ip}")) {
            return lookupUrlTemplate.replace("{ip}", encodedIp);
        }
        return lookupUrlTemplate + encodedIp;
    }

    private JSONObject parseLookupPayload(String responseBody) {
        if (StringUtils.isBlank(responseBody)) {
            return null;
        }
        String trimmedBody = responseBody.trim();
        int jsonStart = trimmedBody.indexOf('{');
        int jsonEnd = trimmedBody.lastIndexOf('}');
        if (jsonStart < 0 || jsonEnd <= jsonStart) {
            return null;
        }
        String jsonText = trimmedBody.substring(jsonStart, jsonEnd + 1);
        return JSONUtil.parseObj(jsonText);
    }

    private String extractSupportedCityFromPayload(JSONObject payload) {
        List<String> candidates = new ArrayList<>();
        candidates.add(payload.getStr("city"));
        candidates.add(payload.getStr("region"));
        candidates.add(payload.getStr("regionNames"));
        candidates.add(payload.getStr("pro"));
        candidates.add(payload.getStr("province"));
        candidates.add(payload.getStr("addr"));
        JSONObject data = payload.getJSONObject("data");
        if (data != null) {
            candidates.add(data.getStr("city"));
            candidates.add(data.getStr("district"));
            candidates.add(data.getStr("prov"));
            candidates.add(data.getStr("province"));
            candidates.add(data.getStr("addr"));
        }
        for (String candidate : candidates) {
            String supportedCity = CityUtils.extractSupportedCity(candidate);
            if (StringUtils.isNotBlank(supportedCity)) {
                return supportedCity;
            }
        }
        return null;
    }
}
