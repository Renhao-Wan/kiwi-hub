package com.iot.kiwiuser.service;

public interface StatsService {
    
    void updateFollowStats(String followerId, String followingId, int delta);
    
    void updateArticleCount(String authorId, int delta);
}