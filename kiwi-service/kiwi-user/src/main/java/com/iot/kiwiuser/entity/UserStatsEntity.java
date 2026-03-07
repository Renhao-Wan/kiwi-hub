package com.iot.kiwiuser.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户统计实体类
 * 
 * <p>对应 MySQL 表 `user_stats`，存储用户实时统计数据。</p>
 * <p>与 {@link UserEntity} 为一对一关系。</p>
 * 
 * @author wan
 */
@Data
@Accessors(chain = true)
@TableName("user_stats")
public class UserStatsEntity {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（外键关联 user.id）
     */
    @TableField("user_id")
    private String userId;

    /**
     * 文章数
     */
    @TableField("article_count")
    private Integer articleCount = 0;

    /**
     * 关注数
     */
    @TableField("following_count")
    private Integer followingCount = 0;

    /**
     * 粉丝数
     */
    @TableField("follower_count")
    private Integer followerCount = 0;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}