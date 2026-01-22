package com.iot.kiwilink;

import com.iot.common.exception.handler.EnableGlobalExceptionHandler;
import com.iot.kiwilink.annotaion.Knife4jConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 短链接服务
 * @author wan
 */
@Knife4jConfig
@EnableGlobalExceptionHandler
@EnableDiscoveryClient
@SpringBootApplication
public class KiwiLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(KiwiLinkApplication.class, args);
    }
}
