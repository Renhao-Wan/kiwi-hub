package com.iot.kiwicontent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论DTO
 * @author wan
 */
@Data
@Schema(description = "评论DTO")
public class CommentDTO {

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", required = true)
    private Long articleId;

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容", required = true)
    private String content;

    @Schema(description = "父评论ID（用于实现盖楼回复结构）")
    private Long parentId;
}