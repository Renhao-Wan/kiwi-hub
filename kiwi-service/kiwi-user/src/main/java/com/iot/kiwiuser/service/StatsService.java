package com.iot.kiwiuser.service;

public interface StatsService {

    void updateFollowStats(Long followerId, Long followingId, int delta);

    void updateArticleCount(Long authorId, int delta);
}