package com.iot.kiwicontent.controller;

import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwicontent.model.dto.ArticleSearchDTO;
import com.iot.kiwicontent.model.vo.ArticleSearchResultVO;
import com.iot.kiwicontent.service.ArticleSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 文章搜索控制器
 * @author wan
 */
@Tag(name = "文章搜索", description = "文章全文检索、正则搜索等相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Validated
public class ArticleSearchController {

    private final ArticleSearchService articleSearchService;

    @Operation(summary = "搜索文章", description = "根据关键词搜索文章，支持全文搜索和正则搜索两种模式")
    @PostMapping("/articles")
    public Result<PageResult<ArticleSearchResultVO>> searchArticles(
            @Parameter(description = "搜索条件", required = true) @RequestBody @Valid ArticleSearchDTO searchDTO) {
        return Result.success(articleSearchService.searchArticles(searchDTO));
    }

    @Operation(summary = "快速搜索文章", description = "使用GET方式快速搜索文章")
    @GetMapping("/articles")
    public Result<PageResult<ArticleSearchResultVO>> quickSearchArticles(
            @Parameter(description = "搜索关键词", required = true) @RequestParam("keyword") String keyword,
            @Parameter(description = "搜索模式：text-全文搜索, regex-正则搜索") @RequestParam(defaultValue = "text") String searchMode,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "排序字段：relevance-相关度, time-时间, views-浏览量, likes-点赞数") @RequestParam(defaultValue = "relevance") String sortBy,
            @Parameter(description = "排序方向：asc-升序, desc-降序") @RequestParam(defaultValue = "desc") String sortOrder,
            @Parameter(description = "是否只搜索标题") @RequestParam(defaultValue = "false") Boolean titleOnly,
            @Parameter(description = "标签过滤（多个标签用逗号分隔）") @RequestParam(required = false) String tags) {
        
        ArticleSearchDTO searchDTO = new ArticleSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setSearchMode(searchMode);
        searchDTO.setPageNum(pageNum);
        searchDTO.setPageSize(pageSize);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortOrder(sortOrder);
        searchDTO.setTitleOnly(titleOnly);
        searchDTO.setTags(tags);
        
        return Result.success(articleSearchService.searchArticles(searchDTO));
    }

    // TODO: 热门搜索关键词接口
    // @Operation(summary = "获取热门搜索关键词", description = "获取最近的热门搜索关键词")
    // @GetMapping("/hot-keywords")
    // public Result<List<String>> getHotKeywords(
    //         @Parameter(description = "返回数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer limit) {
    //     return Result.success(articleSearchService.getHotKeywords(limit));
    // }

    // TODO: 搜索建议接口
    // @Operation(summary = "获取搜索建议", description = "根据输入前缀获取搜索建议")
    // @GetMapping("/suggestions")
    // public Result<List<String>> getSearchSuggestions(
    //         @Parameter(description = "搜索前缀", required = true) @RequestParam("prefix") String prefix,
    //         @Parameter(description = "返回数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer limit) {
    //     return Result.success(articleSearchService.getSearchSuggestions(prefix, limit));
    // }
}