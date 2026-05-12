package com.iot.kiwicontent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文章搜索DTO
 * @author wan
 */
@Data
@Schema(description = "文章搜索DTO")
public class ArticleSearchDTO {

    @NotBlank(message = "搜索关键词不能为空")
    @Schema(description = "搜索关键词", required = true, example = "Java 编程")
    private String keyword;

    @Schema(description = "搜索模式：text-全文搜索, regex-正则搜索", defaultValue = "text", example = "text")
    private String searchMode = "text";

    @Schema(description = "页码", defaultValue = "1", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", defaultValue = "10", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "排序字段：relevance-相关度, time-时间, views-浏览量, likes-点赞数", defaultValue = "relevance", example = "relevance")
    private String sortBy = "relevance";

    @Schema(description = "排序方向：asc-升序, desc-降序", defaultValue = "desc", example = "desc")
    private String sortOrder = "desc";

    @Schema(description = "是否只搜索标题", defaultValue = "false", example = "false")
    private Boolean titleOnly = false;

    @Schema(description = "标签过滤（多个标签用逗号分隔）", example = "Java,Spring")
    private String tags;
}