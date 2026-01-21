package com.iot.kiwicontent.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章实体
 * @author wan
 */
@Data
@Builder
@Schema(description = "文章实体")
@Document(collection = "articles")
public class Article {

    @Id
    @Schema(description = "文章ID")
    private String id;

    @Field("author_id")
    @Schema(description = "作者ID", required = true)
    private String authorId;

    @Schema(description = "文章标题", required = true)
    private String title;

    @Schema(description = "文章内容", required = true)
    private String content;

    @Field("content_type")
    @Schema(description = "内容类型：content-纯文本, html-HTML格式, markdown-Markdown格式", example = "markdown")
    private String contentType;

    @Field("oss_urls")
    @Schema(description = "OSS存储的图片/文件URL列表")
    private List<String> ossUrls;

    @Schema(description = "文章标签列表")
    private List<String> tags;

    @Field("created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Indexed
    @Field("updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "文章统计数据")
    private ArticleStats stats;
}
