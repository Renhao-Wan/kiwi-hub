package com.iot.kiwiuser.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.kiwiuser.entity.UserStatsEntity;
import com.iot.kiwiuser.mapper.UserStatsMapper;
import com.iot.kiwiuser.service.StatsService;
import com.iot.kiwiuser.service.UserStatsEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StatsServiceImpl extends ServiceImpl<UserStatsMapper, UserStatsEntity> implements StatsService, UserStatsEntityService {

    @Async("statsUpdateExecutor")
    @Override
    public void updateFollowStats(Long followerId, Long followingId, int delta) {
        try {
            updateFollowingCount(followerId, delta);
            updateFollowerCount(followingId, delta);
            log.debug("更新关注统计成功: follower={}, following={}, delta={}", followerId, followingId, delta);
        } catch (Exception e) {
            log.error("更新关注统计失败: follower={}, following={}, delta={}", followerId, followingId, delta, e);
        }
    }

    @Async("statsUpdateExecutor")
    @Override
    public void updateArticleCount(Long authorId, int delta) {
        try {
            updateArticleCountInternal(authorId, delta);
            log.debug("更新文章数成功: authorId={}, delta={}", authorId, delta);
        } catch (Exception e) {
            log.error("更新文章数失败: authorId={}, delta={}", authorId, delta, e);
        }
    }

    private void updateFollowingCount(Long userId, int delta) {
        LambdaUpdateWrapper<UserStatsEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserStatsEntity::getUserId, userId)
                .setSql("following_count = following_count + " + delta);
        getBaseMapper().update(null, updateWrapper);
    }

    private void updateFollowerCount(Long userId, int delta) {
        LambdaUpdateWrapper<UserStatsEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserStatsEntity::getUserId, userId)
                .setSql("follower_count = follower_count + " + delta);
        getBaseMapper().update(null, updateWrapper);
    }

    private void updateArticleCountInternal(Long userId, int delta) {
        LambdaUpdateWrapper<UserStatsEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserStatsEntity::getUserId, userId)
                .setSql("article_count = article_count + " + delta);
        getBaseMapper().update(null, updateWrapper);
    }
}