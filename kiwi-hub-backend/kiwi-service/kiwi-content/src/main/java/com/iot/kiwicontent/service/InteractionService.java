package com.iot.kiwicontent.service;

/**
 * 互动服务接口
 * @author wan
 */
public interface InteractionService {
    /**
     * 点赞/取消点赞
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param authorId 作者ID
     * @return 点赞还是取消点赞
     */
    boolean toggleLike(Long userId, Long articleId, Long authorId);
}
