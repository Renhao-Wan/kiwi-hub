package com.iot.kiwicontent.repository;

import com.iot.kiwicontent.model.pojo.ArticleContentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * 文章内容仓库接口
 * 
 * @author wan
 */
@Repository
public interface ArticleContentRepository extends MongoRepository<ArticleContentDocument, Long> {

    /**
     * 根据文章ID查找内容文档
     * @param articleId 文章ID
     * @return 文章内容文档
     */
    ArticleContentDocument findByArticleId(Long articleId);

    /**
     * 根据文章ID删除内容文档
     * @param articleId 文章ID
     */
    void deleteByArticleId(Long articleId);
}