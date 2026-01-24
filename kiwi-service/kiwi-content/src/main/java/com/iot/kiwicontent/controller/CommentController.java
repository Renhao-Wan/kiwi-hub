package com.iot.kiwicontent.controller;

import com.iot.common.context.UserContext;
import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwicontent.model.dto.CommentDTO;
import com.iot.kiwicontent.model.dto.CommentQueryDTO;
import com.iot.kiwicontent.model.vo.CommentVO;
import com.iot.kiwicontent.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论控制器
 * @author wan
 */
@Tag(name = "评论管理", description = "评论发布、删除、查询等相关接口")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "发布评论", description = "发布新评论，支持一级评论和回复评论")
    @PostMapping
    public Result<Object> publishComment(@Parameter(description = "评论信息", required = true) @RequestBody @Valid CommentDTO commentDTO) {
        String userId = UserContext.getUserId();
        return commentService.publishComment(userId, commentDTO);
    }

    @Operation(summary = "删除评论", description = "删除自己的评论（软删除）")
    @DeleteMapping("/{commentId}")
    public Result<Object> deleteComment(@Parameter(description = "评论ID", required = true) @PathVariable("commentId") String commentId) {
        String userId = UserContext.getUserId();
        return commentService.deleteComment(userId, commentId);
    }

    @Operation(summary = "获取文章一级评论列表", description = "分页获取文章的一级评论列表")
    @GetMapping("/roots")
    public Result<PageResult<CommentVO>> getRootComments(
            @Parameter(description = "文章ID", required = true) @RequestParam("articleId") String articleId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(commentService.getRootComments(articleId, pageNum, pageSize));
    }

    @Operation(summary = "获取楼中楼回复列表", description = "使用游标分页获取指定根评论下的所有回复")
    @GetMapping("/replies")
    public Result<Map<String, Object>> getReplies(@Parameter(description = "查询条件", required = true) @ModelAttribute @Valid CommentQueryDTO queryDTO) {
        return commentService.getReplies(queryDTO);
    }
}