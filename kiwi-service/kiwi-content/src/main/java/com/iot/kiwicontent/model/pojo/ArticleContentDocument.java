package com.iot.kiwicontent.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章内容文档（MongoDB存储）
 * 
 * <p>存储文章的半结构化内容数据（title, content, contentType, ossUrls, tags）。</p>
 * <p>与 MySQL 的 article 表通过 articleId 关联（一对一）。冗余存储 authorId, createdAt, updatedAt 便于查询。</p>
 * 
 * @author wan
 */
@Data
@Builder
@Schema(description = "文章内容文档")
@Document(collection = "article_content_cache")
public class ArticleContentDocument {

    /**
     * 文章ID（与 MySQL article.id 一致）
     */
    @Id
    @Schema(description = "文章ID", required = true)
    private String articleId;

    /**
     * 作者ID（冗余，便于查询）
     */
    @Indexed
    @Field("author_id")
    @Schema(description = "作者ID", required = true)
    private String authorId;

    /**
     * 文章标题
     */
    @TextIndexed(weight = 10)
    @Schema(description = "文章标题", required = true)
    private String title;

    /**
     * 文章内容
     */
    @TextIndexed(weight = 3)
    @Schema(description = "文章内容", required = true)
    private String content;

    /**
     * 内容类型：content-纯文本, html-HTML格式, markdown-Markdown格式
     */
    @Field("content_type")
    @Schema(description = "内容类型", example = "markdown")
    private String contentType;

    /**
     * OSS存储的图片/文件URL列表
     */
    @Field("oss_urls")
    @Schema(description = "OSS存储的图片/文件URL列表")
    private List<String> ossUrls;

    /**
     * 文章标签列表
     */
    @TextIndexed(weight = 5)
    @Schema(description = "文章标签列表")
    private List<String> tags;

    /**
     * 创建时间（冗余，便于查询）
     */
    @Indexed
    @Field("created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间（冗余，便于查询）
     */
    @Indexed
    @Field("updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}