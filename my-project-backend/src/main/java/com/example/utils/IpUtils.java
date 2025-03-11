package com.example.utils;

public class IpUtils {
    public static String normalizeIp(String ip) {
        if (ip == null) return "unknown";
        // 处理 IPv6 本地地址
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return "127.0.0.1";
        }
        return ip;
    }
}
