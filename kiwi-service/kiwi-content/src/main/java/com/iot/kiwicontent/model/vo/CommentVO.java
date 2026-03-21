package com.iot.kiwicontent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论VO
 * @author wan
 */
@Data
@Schema(description = "评论VO")
public class CommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "文章ID")
    private Long articleId;

    @Schema(description = "评论者ID")
    private Long authorId;

    @Schema(description = "评论者昵称")
    private String authorName;

    @Schema(description = "评论者头像")
    private String authorAvatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "父评论作者昵称")
    private String parentAuthorName;

    @Schema(description = "根评论ID")
    private Long rootId;

    @Schema(description = "评论状态：0-正常, 1-已删除")
    private Integer status;

    @Schema(description = "评论时间")
    private LocalDateTime createdAt;

    @Schema(description = "三级回复总数（仅二级评论返回此字段）")
    private Long replyCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "当前用户是否点赞")
    private Boolean isLiked;
}