package com.iot.kiwicontent.service.impl;

import com.iot.kiwicontent.model.constant.RedisConstant;
import com.iot.kiwicontent.service.InteractionService;
import com.iot.kiwicontent.service.LikeSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 互动服务实现类
 * @author wan
 */
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final StringRedisTemplate redisTemplate;
    private final LikeSyncService likeSyncService;

    /**
     * 切换点赞状态 (Toggle)
     * @param userId 当前用户ID
     * @param articleId 文章ID
     * @return true=点赞成功, false=取消点赞成功
     */
    @Override
    public boolean toggleLike(String userId, String articleId, String authorId) {
        // 用于存储某篇文章被哪些用户点赞了 (Set 集合)
        String userLikeKey = RedisConstant.USER_LIKE_KEY + articleId;
        // 用于存储文章的总点赞数 (String / Counter)
        String countKey = RedisConstant.LIKE_COUNT_KEY + articleId;

        // 判断用户是否已经点赞 (Redis SISMEMBER 操作，O(1) 复杂度)
        Boolean isLiked = redisTemplate.opsForSet().isMember(userLikeKey, userId);

        if (Boolean.TRUE.equals(isLiked)) {
            // 已经赞过 -> 执行取消点赞 (Unlike)
            // Redis 移除记录
            redisTemplate.opsForSet().remove(userLikeKey, userId);
            // Redis 计数 -1 (必须判断 >0，虽然理论上不会小于0)
            redisTemplate.opsForValue().decrement(countKey);

            // 异步同步到数据库 (替代 RabbitMQ)
            likeSyncService.syncUnlikeToDatabase(userId, articleId);
            // 返回当前状态：未赞
            return false;
        } else {
            // 没赞过 -> 执行点赞 (Like)
            // Redis 添加记录
            redisTemplate.opsForSet().add(userLikeKey, userId);
            // Redis 计数 +1
            redisTemplate.opsForValue().increment(countKey);

            // 异步同步到数据库 (替代 RabbitMQ)
            likeSyncService.syncLikeToDatabase(userId, articleId, authorId);
            // 返回当前状态：已赞
            return true;
        }
    }
}
