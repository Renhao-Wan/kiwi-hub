package com.iot.kiwilink.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.Protocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置类
 * @author wan
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class UpstashConfig {

    @Value("${kiwi.redis.upstash.url}")
    private String redisUrl;

    private final ObjectMapper objectMapper;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        // ------------------------------------------------------------
        // 核心配置：Upstash 专用
        // ------------------------------------------------------------

        // Upstash 对 Redis 6.0/7.0 的 RESP3 协议支持有时候不稳定，RESP2 最稳
        config.setProtocol(Protocol.RESP2);
        config.useSingleServer()
                .setAddress(redisUrl)
                .setDatabase(0)
                .setConnectTimeout(10000)
                .setTimeout(10000)
                // 失败重试次数
                .setRetryAttempts(5)
                .setRetryInterval(2000);
        config.setCodec(new JsonJacksonCodec(objectMapper));

        log.info("正在初始化 Redisson，连接地址: " + config.useSingleServer().getAddress());

        return Redisson.create(config);
    }
}
