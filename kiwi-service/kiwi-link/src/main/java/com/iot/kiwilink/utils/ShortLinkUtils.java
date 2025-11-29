package com.iot.kiwilink.utils;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

/**
 * 短链接工具类
 * @author wan
 */
public class ShortLinkUtils {

    // Base62 字符集
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 10进制转62进制
     */
    public static String toBase62(long num) {
        StringBuilder sb = new StringBuilder();
        if (num == 0) return "0";
        while (num > 0) {
            sb.append(BASE62.charAt((int) (num % 62)));
            num /= 62;
        }
        return sb.reverse().toString();
    }

    /**
     * MurmurHash3 (32-bit) 生成 Hash 值
     * 注意：这里为了防止负数，转为 long 处理
     */
    public static long murmurHash32(String param) {
        long hash = Hashing.murmur3_32_fixed().hashString(param, StandardCharsets.UTF_8).asInt();
        // 保持正数
        return hash < 0 ? Integer.MAX_VALUE - hash : hash;
    }
}
