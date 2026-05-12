package com.iot.kiwiuser.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Session 配置类
 * 默认 Spring Session 使用 JDK 序列化（二进制流），这
 * 会导致如果网关和微服务的类加载器不同，或者没有对应的类，反序列化会报错。
 * 解决这个问题，需要将 Session 序列化为 JSON 格式。
 * @author wan
 */
@Configuration
public class SessionConfig {

    /**
     * 将 Session 序列化为 JSON 格式，而非默认的 JDK 二进制流。
     * 这样不仅跨语言通用，而且通用性更好（GenericJackson2JsonRedisSerializer）。
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer(OBJECT_MAPPER);
    }


    private static final ObjectMapper OBJECT_MAPPER;


    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 支持多态类型处理
        OBJECT_MAPPER.activateDefaultTyping(
                // 使用宽松的多态类型验证器
                LaissezFaireSubTypeValidator.instance,
                // 激活默认类型信息
                ObjectMapper.DefaultTyping.NON_FINAL,
                // 类型信息包含方式为属性
                JsonTypeInfo.As.PROPERTY
        );
        // 设置可见性，确保 private 字段也能被序列化
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        OBJECT_MAPPER.registerModule(javaTimeModule);
    }
}
