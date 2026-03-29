package com.iot.kiwiuser.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 配置类，强制使用根路径作为服务器 URL，解决 Swagger UI 中接口路径错误的问题。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/")))   // 关键这一行：强制使用根路径
                .info(new Info()
                        .title("user 服务 API")    // 根据不同服务改标题
                        .version("v1.0")
                        .description("用户服务接口文档"));
    }
}
