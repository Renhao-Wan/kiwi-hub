package com.iot.kiwilink.controller;

import com.iot.common.result.Result;
import com.iot.kiwilink.service.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author wan
 * 短链接控制器
 */
@Tag(name = "短链接管理", description = "短链接生成、重定向等相关接口")
@RestController
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @Operation(summary = "生成短链接", description = "根据文章ID生成短链接")
    @PostMapping("/links/generate")
    public Result<String> generateShortLink(@Parameter(description = "文章ID", required = true) @RequestParam("articleId") String articleId) {
        return Result.success(linkService.generateShortLink(articleId));
    }


    @Operation(summary = "获取长链接", description = "根据短链接代码获取长链接并重定向，用于统计点击量")
    @GetMapping("/links/s/{code}")
    public void getLongLink(@Parameter(description = "短链接代码", required = true) @PathVariable("code") String code, HttpServletResponse response) throws IOException {
        String longLink = linkService.getLongLink(code);

        if (longLink != null) {
            response.sendRedirect(longLink);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Link not found");
        }
    }
}
