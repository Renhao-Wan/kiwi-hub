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

    @Override
    public boolean toggleLike(Long userId, Long articleId, Long authorId) {
        String userLikeKey = RedisConstant.USER_LIKE_KEY + articleId;
        String countKey = RedisConstant.LIKE_COUNT_KEY + articleId;

        Boolean isLiked = redisTemplate.opsForSet().isMember(userLikeKey, String.valueOf(userId));

        if (Boolean.TRUE.equals(isLiked)) {
            redisTemplate.opsForSet().remove(userLikeKey, String.valueOf(userId));
            redisTemplate.opsForValue().decrement(countKey);
            likeSyncService.syncUnlikeToDatabase(userId, articleId);
            return false;
        } else {
            redisTemplate.opsForSet().add(userLikeKey, String.valueOf(userId));
            redisTemplate.opsForValue().increment(countKey);
            likeSyncService.syncLikeToDatabase(userId, articleId, authorId);
            return true;
        }
    }
}
