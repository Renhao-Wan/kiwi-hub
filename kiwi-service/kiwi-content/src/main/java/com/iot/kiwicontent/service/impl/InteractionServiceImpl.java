package com.iot.kiwicontent.service.impl;

import com.iot.kiwicontent.model.constant.ParameterConstant;
import com.iot.kiwicontent.model.constant.RabbitConstant;
import com.iot.kiwicontent.model.constant.RedisConstant;
import com.iot.kiwicontent.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 互动服务实现类
 * @author wan
 */
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

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

        Map<String, String> msg = new HashMap<>();
        msg.put(ParameterConstant.CURRENT_USER_ID, userId);
        msg.put(ParameterConstant.ARTICLE_ID, articleId);
        msg.put(ParameterConstant.AUTHOR_ID, authorId);

        if (Boolean.TRUE.equals(isLiked)) {
            // 已经赞过 -> 执行取消点赞 (Unlike)
            // Redis 移除记录
            redisTemplate.opsForSet().remove(userLikeKey, userId);
            // Redis 计数 -1 (必须判断 >0，虽然理论上不会小于0)
            redisTemplate.opsForValue().decrement(countKey);

            rabbitTemplate.convertAndSend(RabbitConstant.ARTICLE_INTERACTION_EXCHANGE,
                    userId,
                    msg);
            // 返回当前状态：未赞
            return false;
        } else {
            // 没赞过 -> 执行点赞 (Like)
            // Redis 添加记录
            redisTemplate.opsForSet().add(userLikeKey, userId);
            // Redis 计数 +1
            redisTemplate.opsForValue().increment(countKey);

            rabbitTemplate.convertAndSend(RabbitConstant.ARTICLE_INTERACTION_EXCHANGE,
                    userId,
                    msg);
            // 返回当前状态：已赞
            return true;
        }
    }
}
