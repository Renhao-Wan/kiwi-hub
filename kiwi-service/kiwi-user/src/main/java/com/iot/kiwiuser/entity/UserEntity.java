package com.iot.kiwiuser.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户基础信息实体类
 * 
 * <p>对应 MySQL 表 `user`，存储用户核心认证信息。</p>
 * <p>用户画像数据（avatar_url, bio, tags）存储在 MongoDB 的 user_profiles 集合中。</p>
 * 
 * @author wan
 */
@Data
@Accessors(chain = true)
@TableName("user")
public class UserEntity {

    /**
     * 用户ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 密码哈希值（BCrypt加密）
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 注册时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}