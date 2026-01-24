package com.iot.kiwicontent.controller;

import com.iot.common.context.UserContext;
import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwicontent.model.dto.PublishArticleDTO;
import com.iot.kiwicontent.model.pojo.Article;
import com.iot.kiwicontent.model.vo.ArticleListVO;
import com.iot.kiwicontent.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 文章管理控制器
 * @author wan
 */
@Tag(name = "文章管理", description = "文章发布、删除、查询等相关接口")
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "发布文章", description = "用户发布新文章")
    @PostMapping
    public Result<Object> publishArticle(@Parameter(description = "文章发布信息", required = true) @RequestBody @Validated PublishArticleDTO publishArticleDTO) {
        String userId = UserContext.getUserId();
        articleService.publishArticle(userId, publishArticleDTO);
        return Result.success();
    }

    @Operation(summary = "删除文章", description = "用户删除自己的文章")
    @DeleteMapping
    public Result<Object> deleteArticle(@Parameter(description = "文章ID", required = true) @RequestParam("articleId") String articleId){
        String userId = UserContext.getUserId();
        return articleService.deleteArticle(userId, articleId);
    }

    @Operation(summary = "获取文章列表", description = "分页获取文章列表")
    @GetMapping("/s")
    public Result<PageResult<ArticleListVO>> getArticleList(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                                            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(articleService.getArticleList(pageNum, pageSize, false));
    }

    @Operation(summary = "获取当前用户文章列表", description = "分页获取用户文章列表")
    @GetMapping("/me")
    public Result<PageResult<ArticleListVO>> getMyArticleList(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                                              @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(articleService.getArticleList(pageNum, pageSize, true));
    }

    @Operation(summary = "获取文章详情", description = "根据文章ID获取文章详细信息")
    @GetMapping("{articleId}")
    public Result<Article> getArticleDetail(@Parameter(description = "文章ID", required = true) @PathVariable("articleId") String articleId) {
        return Result.success(articleService.getArticleDetail(articleId));
    }
}
