package com.iot.kiwiuser.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.common.constant.SessionConstant;
import com.iot.common.result.Result;
import com.iot.common.result.ResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
        // 放行 OPTIONS 请求 (CORS 跨域预检请求)
        if (Objects.equals(HttpMethod.OPTIONS.toString(), request.getMethod())) {
            return true;
        }

        // 获取 Session
        // request.getSession(false) 表示：如果有 session 就返回，没有就返回 null，不创建新的
        HttpSession session = request.getSession(false);

        // 校验 Session 是否存在，以及里面有没有用户数据
        if (session != null && session.getAttribute(SessionConstant.LOGIN_USER) != null) {
            // 登录状态正常，放行
            return true;
        }

        // 未登录处理：返回 401 状态码和 JSON 提示
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.result(ResultCodeEnum.UNAUTHORIZED).message("未登录，请先登录");
        response.getWriter().write(objectMapper.writeValueAsString(result));
        // 拦截请求，不再向下执行
        return false;
    }
}
