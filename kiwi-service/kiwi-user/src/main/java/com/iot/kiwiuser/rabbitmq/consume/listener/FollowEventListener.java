package com.iot.kiwiuser.rabbitmq.consume.listener;

import com.iot.kiwiuser.model.constant.ParameterConstant;
import com.iot.kiwiuser.model.constant.RabbitConstant;
import com.iot.kiwiuser.model.pojo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 关注监听者
 * @author wan
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FollowEventListener {

    private static final String FOLLOWER_COUNT = "social_stats.follower_count";
    private static final String FOLLOWING_COUNT = "social_stats.following_count";

    private final MongoTemplate mongoTemplate;

    // 消费者监听消息
    @RabbitListener(queues = RabbitConstant.USER_RELATION_QUEUE)
    public void handleFollowEvent(Map<String, String> message) {
        String followerId = message.get(ParameterConstant.FOLLOWER_ID);
        String followingId = message.get(ParameterConstant.FOLLOWING_ID);
        String action = message.get(ParameterConstant.FOLLOW_ACTION);

        if (ParameterConstant.FOLLOW.equals(action)) {
            updateStats(followerId, followingId, 1);
            log.debug("用户: {} 关注了用户: {}", followerId, followingId);
        } else if (ParameterConstant.UNFOLLOW.equals(action)) {
            updateStats(followerId, followingId, -1);
            log.debug("用户: {} 取消关注了用户: {}", followerId, followingId);
        }
    }

    private void updateStats(String followerId, String followingId, int value) {
        // 关注时 更新 粉丝(follower) 的 '关注数' (followingCount) + 1
        doUpdate(followerId, value, FOLLOWING_COUNT);

        // 关注时 目标(target) 的 '粉丝数' (followerCount) + 1
        doUpdate(followingId, value, FOLLOWER_COUNT);
    }

    private void doUpdate(String followerId, int value, String followingCount) {
        Query query = Query.query(Criteria.where("_id").is(followerId));
        if (value < 0) {
            query.addCriteria(Criteria.where(followingCount).gt(0));
        }
        mongoTemplate.updateFirst(
                query,
                new Update().inc(followingCount, value),
                User.class
        );
    }

}
