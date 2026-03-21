package com.iot.kiwicontent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论查询DTO
 * @author wan
 */
@Data
@Schema(description = "评论查询DTO")
public class CommentQueryDTO {

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", required = true)
    private Long articleId;

    @NotNull(message = "根评论ID不能为空")
    @Schema(description = "根评论ID（一级评论的ID）")
    private Long rootId;

    @NotNull(message = "父评论ID不能为空")
    @Schema(description = "父评论ID（查二级传 rootId，查三级传二级评论的 id）")
    private Long parentId;

    @Schema(description = "游标ID（用于游标分页）")
    private Long cursorId;

    @Schema(description = "每页数量", defaultValue = "20")
    private Integer pageSize = 20;
}