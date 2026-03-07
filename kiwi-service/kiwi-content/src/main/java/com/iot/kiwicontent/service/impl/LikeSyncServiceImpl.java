package com.iot.kiwicontent.service.impl;

import com.iot.kiwicontent.entity.ArticleLikeEntity;
import com.iot.kiwicontent.service.LikeSyncService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.kiwicontent.mapper.ArticleLikeMapper;
import com.iot.kiwicontent.service.ArticleLikeEntityService;
import com.iot.kiwicontent.service.ArticleStatsEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 点赞同步服务实现类（MySQL 存储）
 * 
 * @author wan
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeSyncServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLikeEntity> implements LikeSyncService, ArticleLikeEntityService {


    private final ArticleStatsEntityService articleStatsEntityService;

    /**
     * 同步点赞到数据库（异步）
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param authorId 作者ID
     */
    @Async("interactionExecutor")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncLikeToDatabase(String userId, String articleId, String authorId) {
        try {
            ArticleLikeEntity like = new ArticleLikeEntity()
                    .setUserId(userId)
                    .setArticleId(articleId)
                    .setAuthorId(authorId)
                    .setCreatedAt(LocalDateTime.now());
            save(like);

            // 更新文章点赞计数
            articleStatsEntityService.updateLikeCount(articleId, 1);

            log.info("点赞数据同步完成: u={} -> a={}", userId, articleId);
        } catch (DataIntegrityViolationException e) {
            log.warn("重复点赞，已忽略: u={}, a={}", userId, articleId);
        } catch (Exception e) {
            log.error("点赞同步失败: u={}, a={}", userId, articleId, e);
        }
    }

    /**
     * 同步取消点赞到数据库（异步）
     * @param userId 用户ID
     * @param articleId 文章ID
     */
    @Async("interactionExecutor")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUnlikeToDatabase(String userId, String articleId) {
        try {
            // 删除点赞记录
            boolean removed = removeByUserIdAndArticleId(userId, articleId);
            if (removed) {
                // 更新文章点赞计数
                articleStatsEntityService.updateLikeCount(articleId, -1);
                log.info("取消点赞同步完成: u={} -> a={}", userId, articleId);

}
        } catch (Exception e) {
            log.error("取消点赞同步失败: u={}, a={}", userId, articleId, e);
        }
    }

    @Override
    public boolean removeByUserIdAndArticleId(String userId, String articleId) {
        LambdaQueryWrapper<ArticleLikeEntity> wrapper = new LambdaQueryWrapper<ArticleLikeEntity>()
                .eq(ArticleLikeEntity::getUserId, userId)
                .eq(ArticleLikeEntity::getArticleId, articleId);
        return remove(wrapper);
    }
}