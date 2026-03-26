package com.xduo.springbootinit.blackfilter;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 黑名单过滤工具类
 */
@Slf4j
public class BlackIpUtils {

    // 布隆过滤器，用于快速初筛
    private static BitMapBloomFilter bloomFilter = new BitMapBloomFilter(10);

    // 真正的黑名单 Set，用于二级校验，保证 100% 准确
    private static volatile Set<String> blackIpSet = Collections.emptySet();

    /**
     * 判断 ip 是否在黑名单内
     *
     * @param ip
     * @return
     */
    public static boolean isBlackIp(String ip) {
        // 1. 布隆过滤器快速初筛
        if (!bloomFilter.contains(ip)) {
            return false;
        }
        // 2. 二级校验，确保 100% 准确性，防止误判
        return blackIpSet.contains(ip);
    }

    /**
     * 重建 ip 黑名单
     *
     * @param configInfo
     */
    public static void rebuildBlackIp(String configInfo) {
        if (StrUtil.isBlank(configInfo)) {
            configInfo = "{}";
        }
        // 解析 yaml 文件
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(configInfo, Map.class);
        // 获取 ip 黑名单
        List<String> blackIpList = (List<String>) map.get("blackIpList");
        // 加锁防止并发重建
        synchronized (BlackIpUtils.class) {
            if (CollectionUtil.isNotEmpty(blackIpList)) {
                // 重建布隆过滤器 (100MB 空间)
                BitMapBloomFilter bitMapBloomFilter = new BitMapBloomFilter(100);
                // 重建精准 Set
                Set<String> newBlackIpSet = new HashSet<>();
                for (String ip : blackIpList) {
                    bitMapBloomFilter.add(ip);
                    newBlackIpSet.add(ip);
                }
                bloomFilter = bitMapBloomFilter;
                blackIpSet = newBlackIpSet;
                log.info("黑名单更新成功，数量：{}", blackIpSet.size());
            } else {
                bloomFilter = new BitMapBloomFilter(10);
                blackIpSet = Collections.emptySet();
            }
        }
    }
}
