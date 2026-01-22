package com.iot.kiwicontent.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.common.context.UserContext;
import com.iot.common.result.Result;
import com.iot.common.result.ResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * 登录拦截器
 * @author wan
 */
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if (Objects.equals(HttpMethod.OPTIONS.toString(), request.getMethod())) {
            return true;
        }

        String userId = UserContext.getUserId();
        if (userId != null) {
            return true;
        }

        // 未登录，返回 401
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.result(ResultCodeEnum.UNAUTHORIZED).message("未登录，请先登录");
        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }
}