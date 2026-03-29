package com.xduo.springbootinit.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于请求头解析用户城市
 */
@Component
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
        return null;
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
}
