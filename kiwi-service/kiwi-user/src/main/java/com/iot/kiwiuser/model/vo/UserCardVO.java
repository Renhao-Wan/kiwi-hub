package com.iot.kiwiuser.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户卡片视图对象
 * @author wan
 */
@Schema(description = "用户卡片视图对象")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCardVO {
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "邮箱地址（已脱敏）", example = "joh****@example.com")
    private String email;

    @Schema(description = "关注时间", example = "2024-01-15T10:30:00")
    private LocalDateTime followTime;

    public String getEmail() {
        String[] emailParts = email.split("@");
        String username = emailParts[0];
        String domain = emailParts[1];

        String maskedUsername = username.length() <= 3
                ? username + "****"
                : username.substring(0, 3) + "****";

        return maskedUsername + "@" + domain;
    }

}
