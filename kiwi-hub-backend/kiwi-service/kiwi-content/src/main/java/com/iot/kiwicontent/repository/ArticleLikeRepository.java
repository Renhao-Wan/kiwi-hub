package com.iot.kiwicontent.repository;

import com.iot.kiwicontent.model.pojo.ArticleLike;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 文章点赞仓库
 * @author wan
 */
public interface ArticleLikeRepository extends MongoRepository<ArticleLike, String> {

    /**
     * 根据用户ID和文章ID删除点赞记录
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 删除的记录数
     */
    long deleteByUserIdAndArticleId(Long userId, Long articleId);
}
