package com.iot.kiwicontent.service.impl;

import com.iot.kiwicontent.model.pojo.Article;
import com.iot.kiwicontent.model.pojo.ArticleLike;
import com.iot.kiwicontent.repository.ArticleLikeRepository;
import com.iot.kiwicontent.service.LikeSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeSyncServiceImpl implements LikeSyncService {

    private final ArticleLikeRepository articleLikeRepository;
    private final MongoTemplate mongoTemplate;

    // TODO 暂未考虑事务
    @Async("interactionExecutor")
    @Override
    public void syncLikeToDatabase(String userId, String articleId, String authorId) {
        try {
            ArticleLike like = new ArticleLike(userId, articleId, authorId);
            articleLikeRepository.save(like);
            incrementLikeCount(articleId);
            log.info("点赞数据同步完成: u={} -> a={}", userId, articleId);
        } catch (DataIntegrityViolationException e) {
            log.warn("重复点赞，已忽略: u={}, a={}", userId, articleId);
        } catch (Exception e) {
            log.error("点赞同步失败: u={}, a={}", userId, articleId, e);
        }
    }

    // TODO 暂未考虑事务
    @Async("interactionExecutor")
    @Override
    public void syncUnlikeToDatabase(String userId, String articleId) {
        try {
            long deleted = articleLikeRepository.deleteByUserIdAndArticleId(userId, articleId);
            if (deleted > 0) {
                decrementLikeCount(articleId);
                log.info("取消点赞同步完成: u={} -> a={}", userId, articleId);
            }
        } catch (Exception e) {
            log.error("取消点赞同步失败: u={}, a={}", userId, articleId, e);
        }
    }

    private void incrementLikeCount(String articleId) {
        Query query = new Query(Criteria.where("_id").is(articleId));
        Update update = new Update().inc("stats.like_count", 1);
        mongoTemplate.updateFirst(query, update, Article.class);
    }

    private void decrementLikeCount(String articleId) {
        Query query = new Query(Criteria.where("_id").is(articleId));
        Update update = new Update().inc("stats.like_count", -1);
        mongoTemplate.updateFirst(query, update, Article.class);
    }
}