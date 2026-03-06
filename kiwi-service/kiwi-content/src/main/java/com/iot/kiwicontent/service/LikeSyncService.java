package com.iot.kiwicontent.service;

public interface LikeSyncService {
    
    void syncLikeToDatabase(String userId, String articleId, String authorId);
    
    void syncUnlikeToDatabase(String userId, String articleId);
}