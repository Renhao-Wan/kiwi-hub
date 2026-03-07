package com.iot.kiwicontent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 评论实体类
 * 
 * <p>对应 MySQL 表 `comment`，存储文章评论数据。</p>
 * <p>支持盖楼回复结构（parent_id, root_id）。</p>
 * 
 * @author wan
 */
@Data
@Accessors(chain = true)
@TableName("comment")
public class CommentEntity {

    /**
     * 评论ID (UUID)
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 文章ID（外键关联 article.id）
     */
    @TableField("article_id")
    private String articleId;

    /**
     * 评论者ID（外键关联 kiwi_user.user.id）
     */
    @TableField("author_id")
    private String authorId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 父评论ID（用于实现盖楼回复结构）
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 根评论ID（用于快速定位评论串的根节点）
     */
    @TableField("root_id")
    private String rootId;

    /**
     * 评论状态：0-正常, 1-已删除
     */
    @TableField("status")
    private Integer status = 0;

    /**
     * 评论时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}