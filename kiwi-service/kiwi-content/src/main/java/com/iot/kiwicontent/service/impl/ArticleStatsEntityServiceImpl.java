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
    public boolean updateCommentCount(String articleId, int delta) {
        return updateCount(articleId, "comment_count", delta);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLikeCount(String articleId, int delta) {
        return updateCount(articleId, "like_count", delta);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateViewCount(String articleId, int delta) {
        return updateCount(articleId, "view_count", delta);
    }

    /**
     * 通用计数更新方法
     * @param articleId 文章ID
     * @param column 列名（comment_count, like_count, view_count）
     * @param delta 变化量
     * @return 是否更新成功
     */
    private boolean updateCount(String articleId, String column, int delta) {
        // 先尝试更新现有记录
        LambdaUpdateWrapper<ArticleStatsEntity> wrapper = new LambdaUpdateWrapper<ArticleStatsEntity>()
                .eq(ArticleStatsEntity::getArticleId, articleId)
                .setSql(column + " = " + column + " + " + delta)
                .set(ArticleStatsEntity::getUpdatedAt, LocalDateTime.now());

        boolean updated = update(wrapper);
        if (updated) {
            return true;
        }

        // 如果记录不存在，则创建一条（可能发生在文章刚刚创建但尚未初始化统计时）
        ArticleStatsEntity stats = new ArticleStatsEntity()
                .setArticleId(articleId)
                .setViewCount(0)
                .setLikeCount(0)
                .setCommentCount(0)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());

        // 根据列名设置初始值
        switch (column) {
            case "comment_count":
                stats.setCommentCount(Math.max(delta, 0));
                break;
            case "like_count":
                stats.setLikeCount(Math.max(delta, 0));
                break;
            case "view_count":
                stats.setViewCount(Math.max(delta, 0));
                break;
            default:
                // 保持默认值
        }

        return save(stats);
    }
}