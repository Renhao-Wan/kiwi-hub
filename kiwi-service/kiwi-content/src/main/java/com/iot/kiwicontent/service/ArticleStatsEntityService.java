package com.iot.kiwicontent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iot.kiwicontent.entity.ArticleStatsEntity;

/**
 * 文章统计数据 Service 接口
 * 
 * <p>继承 MyBatis-Plus 的 IService，提供文章统计数据表的基础 CRUD 操作。</p>
 * 
 * @author wan
 */
public interface ArticleStatsEntityService extends IService<ArticleStatsEntity> {

    /**
     * 更新文章评论计数
     * @param articleId 文章ID
     * @param delta 变化量（正数增加，负数减少）
     * @return 是否更新成功
     */
    boolean updateCommentCount(String articleId, int delta);

    /**
     * 更新文章点赞计数
     * @param articleId 文章ID
     * @param delta 变化量（正数增加，负数减少）
     * @return 是否更新成功
     */
    boolean updateLikeCount(String articleId, int delta);

    /**
     * 更新文章浏览计数
     * @param articleId 文章ID
     * @param delta 变化量（正数增加，负数减少）
     * @return 是否更新成功
     */
    boolean updateViewCount(String articleId, int delta);
}