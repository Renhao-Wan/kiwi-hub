package com.iot.kiwilink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 短链接配置
 * @author wan
 */
@Data
@ConfigurationProperties("link.url")
@Configuration
public class LinkUrlProperties {
    private String baseShortLink = "http://127.0.0.1/links/s/";
    private String baseLongLink = "http://127.0.0.1/articles/";
}
