package com.iot.kiwicontent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 文章统计实体类
 * 
 * <p>对应 MySQL 表 `article_stats`，存储文章实时互动数据。</p>
 * <p>与 {@link ArticleEntity} 为一对一关系。</p>
 * 
 * @author wan
 */
@Data
@Accessors(chain = true)
@TableName("article_stats")
public class ArticleStatsEntity {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID（外键关联 article.id）
     */
    @TableField("article_id")
    private String articleId;

    /**
     * 浏览量
     */
    @TableField("view_count")
    private Integer viewCount = 0;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount = 0;

    /**
     * 评论数
     */
    @TableField("comment_count")
    private Integer commentCount = 0;

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