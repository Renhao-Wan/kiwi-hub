package com.iot.common.context;

import java.util.Optional;

/**
 * 用户上下文
 * @author wan
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static Optional<Long> getOptionalUserId() {
        return Optional.ofNullable(USER_ID_HOLDER.get());
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
