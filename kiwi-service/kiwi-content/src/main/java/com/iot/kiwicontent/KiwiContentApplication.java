package com.iot.kiwicontent;

import com.iot.common.exception.handler.EnableGlobalExceptionHandler;
import com.iot.kiwicontent.annotaion.Knife4jConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启动类
 * @author wan
 */
@Knife4jConfig
@EnableGlobalExceptionHandler
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class KiwiContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(KiwiContentApplication.class, args);
    }
}
