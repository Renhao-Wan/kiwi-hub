package com.iot.kiwiuser;

import com.iot.common.annotaion.Knife4jConfig;
import com.iot.common.exception.handler.EnableGlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author wan
 */
@Knife4jConfig
@EnableGlobalExceptionHandler
@EnableDiscoveryClient
@SpringBootApplication
public class KiwiUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(KiwiUserApplication.class, args);
    }
}
