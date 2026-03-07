package com.iot.kiwiuser.controller;

import com.iot.common.result.Result;
import com.iot.kiwiuser.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计控制器
 * @author wan
 */
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 更新文章数量
     * @param userId 用户ID
     * @param delta 增量
     * @return 是否成功
     */
    @PostMapping("/article/count/{userId}/{delta}")
    public Result<Object> updateArticleCount(@PathVariable String userId, @PathVariable int delta) {
        statsService.updateArticleCount(userId, delta);
        return Result.success();
    }
}