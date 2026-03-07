package com.iot.kiwiuser.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体类
 * 
 * <p>对应 MySQL 表 `user_relation`，存储用户间的关注关系。</p>
 * <p>数据库层面有唯一约束 (follower_id, following_id) 防止重复关注。</p>
 * 
 * @author wan
 */
@Data
@Accessors(chain = true)
@TableName("user_relation")
public class UserRelationEntity {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关注者ID（外键关联 user.id）
     */
    @TableField("follower_id")
    private String followerId;

    /**
     * 被关注者ID（外键关联 user.id）
     */
    @TableField("following_id")
    private String followingId;

    /**
     * 关注时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}