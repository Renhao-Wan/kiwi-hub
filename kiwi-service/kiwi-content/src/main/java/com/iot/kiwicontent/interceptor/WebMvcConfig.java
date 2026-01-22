package com.iot.kiwicontent.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc 配置类
 * @author wan
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private final ObjectMapper objectMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(objectMapper))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 公开接口：查看文章、评论、搜索（只读操作）
                        "/articles/s",           // 获取文章列表
                        "/articles/{articleId}", // 获取文章详情
                        "/comments/roots",       // 获取一级评论列表
                        "/comments/replies",     // 获取楼中楼回复列表
                        "/search/**",            // 搜索相关接口
                        // Swagger 文档相关
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/favicon.ico"
                );
    }
}