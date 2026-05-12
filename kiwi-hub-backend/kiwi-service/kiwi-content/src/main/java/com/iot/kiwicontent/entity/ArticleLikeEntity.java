package com.iot.kiwicontent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 文章点赞实体类
 * 
 * <p>对应 MySQL 表 `article_like`，存储文章点赞记录。</p>
 * <p>数据库层面有唯一约束 (user_id, article_id) 防止重复点赞。</p>
 * 
 * @author wan
 */
@Data
@Accessors(chain = true)
@TableName("article_like")
public class ArticleLikeEntity {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 点赞用户ID（外键关联 kiwi_user.user.id）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 文章ID（外键关联 article.id）
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 文章作者ID（冗余字段，方便快速统计）
     */
    @TableField("author_id")
    private Long authorId;

    /**
     * 点赞时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}