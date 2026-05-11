package com.iot.kiwicontent.service;

public interface LikeSyncService {

    void syncLikeToDatabase(Long userId, Long articleId, Long authorId);

    void syncUnlikeToDatabase(Long userId, Long articleId);
}