package com.xduo.springbootinit.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 城市统一处理工具
 */
public final class CityUtils {

    private static final Set<String> SUPPORTED_CITY_SET = new LinkedHashSet<>();

    static {
        // 直辖市 / 特别行政区
        addSupportedCities("北京", "上海", "天津", "重庆", "香港", "澳门");
        // 华北 / 东北
        addSupportedCities("石家庄", "太原", "呼和浩特", "沈阳", "大连", "长春", "哈尔滨");
        // 华东
        addSupportedCities("南京", "苏州", "无锡", "杭州", "宁波", "合肥", "福州", "厦门", "南昌", "济南", "青岛");
        // 华中 / 华南
        addSupportedCities("郑州", "武汉", "长沙", "广州", "深圳", "佛山", "东莞", "南宁", "海口");
        // 西南 / 西北
        addSupportedCities("成都", "贵阳", "昆明", "拉萨", "西安", "兰州", "西宁", "银川", "乌鲁木齐");
    }

    private CityUtils() {
    }

    private static void addSupportedCities(String... cities) {
        for (String city : cities) {
            SUPPORTED_CITY_SET.add(city);
        }
    }

    public static String normalizeCity(String city) {
        if (city == null) {
            return null;
        }
        String normalizedCity = city.trim();
        if (StringUtils.isBlank(normalizedCity)) {
            return null;
        }
        if (normalizedCity.endsWith("市")) {
            normalizedCity = normalizedCity.substring(0, normalizedCity.length() - 1);
        }
        return normalizedCity;
    }

    public static boolean isSupportedCity(String city) {
        String normalizedCity = normalizeCity(city);
        return normalizedCity != null && SUPPORTED_CITY_SET.contains(normalizedCity);
    }

    public static String normalizeSupportedCity(String city) {
        String normalizedCity = normalizeCity(city);
        if (normalizedCity == null) {
            return null;
        }
        return isSupportedCity(normalizedCity) ? normalizedCity : null;
    }

    public static String extractSupportedCity(String locationText) {
        String normalizedCity = normalizeSupportedCity(locationText);
        if (normalizedCity != null) {
            return normalizedCity;
        }
        String normalizedLocationText = normalizeCity(locationText);
        if (normalizedLocationText == null) {
            return null;
        }
        String compactLocationText = normalizedLocationText.replace(" ", "");
        for (String supportedCity : SUPPORTED_CITY_SET) {
            if (compactLocationText.contains(supportedCity)) {
                return supportedCity;
            }
        }
        return null;
    }
}
