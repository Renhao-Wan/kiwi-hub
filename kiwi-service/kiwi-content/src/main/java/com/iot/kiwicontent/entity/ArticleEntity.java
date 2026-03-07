package com.iot.kiwicontent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 文章基础信息实体类
 * 
 * <p>对应 MySQL 表 `article`，存储文章核心元数据。</p>
 * <p>文章内容（content, contentType, ossUrls, tags）存储在 MongoDB 的 article_content_cache 集合中。</p>
 * 
 * @author wan
 */
@Data
@Accessors(chain = true)
@TableName("article")
public class ArticleEntity {

    /**
     * 文章ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 作者ID（外键关联 kiwi_user.user.id）
     */
    @TableField("author_id")
    private String authorId;

    /**
     * 文章标题
     */
    @TableField("title")
    private String title;

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

    /**
     * 逻辑删除标志（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}