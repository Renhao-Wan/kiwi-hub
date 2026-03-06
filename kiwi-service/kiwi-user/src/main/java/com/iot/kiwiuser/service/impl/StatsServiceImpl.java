package com.iot.kiwiuser.service.impl;

import com.iot.kiwiuser.model.pojo.User;
import com.iot.kiwiuser.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final MongoTemplate mongoTemplate;

    // TODO 暂未考虑事务
    @Async("statsUpdateExecutor")
    @Override
    public void updateFollowStats(String followerId, String followingId, int delta) {
        try {
            updateFollowingCount(followerId, delta);
            updateFollowerCount(followingId, delta);
            log.debug("更新关注统计成功: follower={}, following={}, delta={}", 
                followerId, followingId, delta);
        } catch (Exception e) {
            log.error("更新关注统计失败: follower={}, following={}, delta={}", 
                followerId, followingId, delta, e);
        }
    }

    // TODO 暂未考虑事务
    @Async("statsUpdateExecutor")
    @Override
    public void updateArticleCount(String authorId, int delta) {
        try {
            updateArticleCountInternal(authorId, delta);
            log.debug("更新文章数成功: authorId={}, delta={}", authorId, delta);
        } catch (Exception e) {
            log.error("更新文章数失败: authorId={}, delta={}", authorId, delta, e);
        }
    }

    private void updateFollowingCount(String userId, int delta) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().inc("social_stats.following_count", delta);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    private void updateFollowerCount(String userId, int delta) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().inc("social_stats.follower_count", delta);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    private void updateArticleCountInternal(String userId, int delta) {
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update().inc("social_stats.article_count", delta);
        mongoTemplate.updateFirst(query, update, User.class);
    }
}