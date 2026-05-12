package com.iot.kiwicontent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章搜索结果VO
 * @author wan
 */
@Data
@Schema(description = "文章搜索结果VO")
public class ArticleSearchResultVO {

    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "作者ID")
    private Long authorId;

    @Schema(description = "作者昵称")
    private String authorName;

    @Schema(description = "作者头像")
    private String authorAvatar;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要（搜索关键词高亮）")
    private String summary;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "文章标签列表")
    private List<String> tags;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "相关度评分（仅全文搜索时有效）")
    private Double relevanceScore;

    @Schema(description = "搜索关键词高亮片段列表")
    private List<String> highlights;
}