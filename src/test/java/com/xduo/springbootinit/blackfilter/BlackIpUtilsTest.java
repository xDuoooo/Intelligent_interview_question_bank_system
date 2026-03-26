package com.xduo.springbootinit.blackfilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlackIpUtilsTest {

    @Test
    public void testRebuildBlackIp() {
        String configInfo = "blackIpList:\n" +
                "  - 127.0.0.1\n" +
                "  - 192.168.1.1";
        BlackIpUtils.rebuildBlackIp(configInfo);
        Assertions.assertTrue(BlackIpUtils.isBlackIp("127.0.0.1"));
        Assertions.assertTrue(BlackIpUtils.isBlackIp("192.168.1.1"));
        Assertions.assertFalse(BlackIpUtils.isBlackIp("127.0.0.2"));
    }

    @Test
    public void testRebuildBlackIpEmpty() {
        BlackIpUtils.rebuildBlackIp("");
        Assertions.assertFalse(BlackIpUtils.isBlackIp("127.0.0.1"));

        BlackIpUtils.rebuildBlackIp(null);
        Assertions.assertFalse(BlackIpUtils.isBlackIp("127.0.0.1"));
        
        BlackIpUtils.rebuildBlackIp("{}");
        Assertions.assertFalse(BlackIpUtils.isBlackIp("127.0.0.1"));
    }
}
