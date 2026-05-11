package com.iot.kiwicontent.filter;

import com.iot.common.context.UserContext;
import com.iot.common.constant.SessionConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 用户上下文过滤器
 * @author wan
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object loginUser = session.getAttribute(SessionConstant.LOGIN_USER);
                if (loginUser instanceof Map<?, ?> userMap) {
                    Object userId = userMap.get(SessionConstant.ATTRIBUTE_ID);
                    if (userId instanceof Long id) {
                        UserContext.setUserId(id);
                    } else if (userId instanceof String s) {
                        try {
                            UserContext.setUserId(Long.parseLong(s));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
