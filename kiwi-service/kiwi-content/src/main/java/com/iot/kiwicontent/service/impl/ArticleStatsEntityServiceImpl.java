package com.iot.kiwicontent.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.kiwicontent.entity.ArticleStatsEntity;
import com.iot.kiwicontent.mapper.ArticleStatsMapper;
import com.iot.kiwicontent.service.ArticleStatsEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 文章统计数据 Service 实现类
 * 
 * <p>继承 MyBatis-Plus 的 ServiceImpl，提供文章统计数据表的基础 CRUD 操作实现。</p>
 * 
 * @author wan
 */
@Service
public class ArticleStatsEntityServiceImpl extends ServiceImpl<ArticleStatsMapper, ArticleStatsEntity> implements ArticleStatsEntityService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCommentCount(Long articleId, int delta) {
        return updateCount(articleId, "comment_count", delta);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLikeCount(Long articleId, int delta) {
        return updateCount(articleId, "like_count", delta);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateViewCount(Long articleId, int delta) {
        return updateCount(articleId, "view_count", delta);
    }

    /**
     * 通用计数更新方法
     * @param articleId 文章ID
     * @param column 列名（comment_count, like_count, view_count）
     * @param delta 变化量
     * @return 是否更新成功
     */
    private boolean updateCount(Long articleId, String column, int delta) {
        LambdaUpdateWrapper<ArticleStatsEntity> wrapper = new LambdaUpdateWrapper<ArticleStatsEntity>()
                .eq(ArticleStatsEntity::getArticleId, articleId)
                .setSql(column + " = " + column + " + " + delta)
                .set(ArticleStatsEntity::getUpdatedAt, LocalDateTime.now());

        boolean updated = update(wrapper);
        if (updated) {
            return true;
        }

        ArticleStatsEntity stats = new ArticleStatsEntity()
                .setArticleId(articleId)
                .setViewCount(0)
                .setLikeCount(0)
                .setCommentCount(0)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());

        switch (column) {
            case "comment_count" -> stats.setCommentCount(Math.max(delta, 0));
            case "like_count" -> stats.setLikeCount(Math.max(delta, 0));
            case "view_count" -> stats.setViewCount(Math.max(delta, 0));
            default -> { }
        }

        return save(stats);
    }
}