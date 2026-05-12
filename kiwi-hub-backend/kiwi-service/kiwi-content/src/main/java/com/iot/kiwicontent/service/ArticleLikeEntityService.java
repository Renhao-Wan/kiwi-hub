package com.iot.kiwicontent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iot.kiwicontent.entity.ArticleLikeEntity;

/**
 * 文章点赞 Service 接口
 * 
 * <p>继承 MyBatis-Plus 的 IService，提供文章点赞表的基础 CRUD 操作。</p>
 * 
 * @author wan
 */
public interface ArticleLikeEntityService extends IService<ArticleLikeEntity> {

    /**
     * 根据用户ID和文章ID删除点赞记录
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 是否删除成功
     */
    boolean removeByUserIdAndArticleId(Long userId, Long articleId);
}