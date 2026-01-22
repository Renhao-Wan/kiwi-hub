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
    private String articleId;

    @Schema(description = "根评论ID（用于查询楼中楼回复）")
    private String rootId;

    @Schema(description = "游标ID（用于游标分页）")
    private String cursorId;

    @Schema(description = "每页数量", defaultValue = "20")
    private Integer pageSize = 20;
}