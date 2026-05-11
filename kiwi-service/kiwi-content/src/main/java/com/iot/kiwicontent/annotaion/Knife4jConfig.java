package com.iot.kiwicontent.annotaion;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Knife4j 配置注解
 * @author wan
 */
@OpenAPIDefinition(
        info = @Info(
                title = "内容服务API",
                description = "内容相关的接口文档",
                version = "v1.0",
                contact = @Contact(
                        name = "Renhao-Wan",
                        url = "https://github.com/Renhao-Wan",
                        email = "2653990@qq.com"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8070",
                        description = "本地环境"
                ),
                @Server(
                        url = "http://120.27.158.5:8070",
                        description = "云环境"
                ),
        }
)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Knife4jConfig {
}
