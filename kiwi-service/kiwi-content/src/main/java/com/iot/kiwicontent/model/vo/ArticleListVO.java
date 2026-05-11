package com.iot.kiwicontent.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wan
 * 文章列表视图对象
 */
@Schema(description = "文章列表视图对象")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleListVO {
    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "文章标题", example = "我的第一篇文章")
    private String title;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "内容类型")
    private String contentType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "文章标签列表")
    private List<String> tags;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间", example = "2024-01-15 10:30:00")
    private LocalDateTime updatedAt;
}
