package com.iot.kiwicontent.controller;

import com.iot.common.context.UserContext;
import com.iot.common.result.Result;
import com.iot.kiwicontent.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 互动控制器
 * @author wan
 */
@Tag(name = "互动管理", description = "文章点赞等互动相关接口")
@RestController
@RequestMapping("/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @Operation(summary = "点赞/取消点赞", description = "对指定文章进行点赞或取消点赞操作")
    @PostMapping("/like")
    public Result<Object> toggleLike(@Parameter(description = "文章ID", required = true) @RequestParam("articleId") String articleId,
                               @Parameter(description = "作者ID", required = true) @RequestParam("authorId") String authorId) {
        String userId = UserContext.getUserId();
        boolean isLiked = interactionService.toggleLike(userId, articleId, authorId);
        Map<String, Object> data = new HashMap<>();
        data.put("isLiked", isLiked);
        return Result.success(data);
    }
}
