package com.iot.kiwilink.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置类
 * @author wan
 */
@Configuration
public class RBloomFilterConfig {

    private static final int EXPECTED_INSERTIONS = 100000;
    private static final double FALSE_POSITIVE_PROBABILITY = 0.01;
    private static final String BLOOM_FILTER_KEY = "bloom:filter:shortLink:code";


    @Bean
    public RBloomFilter<String> bloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_KEY);
        bloomFilter.tryInit(EXPECTED_INSERTIONS, FALSE_POSITIVE_PROBABILITY);
        return bloomFilter;
    }
}
