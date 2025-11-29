package com.iot.kiwilink.controller;

import com.iot.common.result.Result;
import com.iot.kiwilink.service.LinkService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 短链接控制器
 * @author wan
 */
@RestController
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    /**
     * 生成短链接
     * @param articleId 文章ID
     * @return 短链接
     */
    @PostMapping("/links/generate")
    public Result<String> generateShortLink(@RequestParam("articleId") String articleId) {
        return Result.success(linkService.generateShortLink(articleId));
    }


    /**
     * 获取长链接
     * 高速重定向。查询 Redis 映射，返回 `302 Found`。
     * @param code 短链接
     */
    @GetMapping("/links/s/{code}")
    public void getLongLink(@PathVariable("code") String code, HttpServletResponse response) throws IOException {
        String longLink = linkService.getLongLink(code);

        if (longLink != null) {
            // 302 临时重定向 (浏览器不会缓存太久，方便统计数据)
            // 如果是 301 永久重定向，浏览器会缓存，下次不经过你的服务器直接跳，无法统计点击量
            response.sendRedirect(longLink);
        } else {
            // 没找到，返回 404 或跳回首页
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Link not found");
        }
    }
}
