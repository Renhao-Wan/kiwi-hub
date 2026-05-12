package com.iot.kiwicontent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发布文章请求参数
 * @author wan
 */
@Schema(description = "发布文章请求参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublishArticleDTO {
    @NotBlank(message = "标题不能为空")
    @Schema(description = "文章标题", required = true, example = "我的第一篇文章")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "文章内容", required = true, example = "这是文章的内容...")
    private String content;

    @Schema(description = "内容类型：content-纯文本, html-HTML格式, markdown-Markdown格式", example = "markdown")
    private String contentType;

    @Schema(description = "OSS存储的图片/文件URL列表")
    private List<String> ossUrls;

    @Schema(description = "文章标签列表")
    private List<String> tags;
}
