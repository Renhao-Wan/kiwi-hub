package com.iot.kiwiuser.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * @author wan
 */
@Schema(description = "用户实体")
@Data
@Builder
@Document(collection = "users")
public class User {

    @Id
    @Schema(description = "用户ID")
    private String id;

    @Indexed(unique = true)
    @Schema(description = "用户名", required = true)
    private String username;

    @Indexed(unique = true)
    @Schema(description = "邮箱", required = true)
    private String email;

    @Field(name = "password_hash")
    @Schema(description = "密码哈希值（BCrypt加密）", required = true)
    private String passwordHash;

    @Field(name = "created_at")
    @Schema(description = "注册时间")
    private LocalDateTime createdAt;

    @Schema(description = "用户资料信息")
    private UserProfile profile;

    @Field(name = "social_stats")
    @Schema(description = "用户社交统计数据")
    private UserStats socialStats;
}