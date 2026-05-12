package com.iot.kiwicontent.repository;

import com.iot.kiwicontent.model.pojo.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * 文章仓库接口
 * @author wan
 */
public interface ArticleRepository extends MongoRepository<Article, String> {
    /**
     * 根据作者ID查询文章列表
     *
     * @param userId 作者ID
     * @param pageable 分页参数
     * @return 文章列表
     */
    @Query(
            value = "{'authorId': ?0}",
            sort = "{ 'updatedAt': -1 }",
            fields = "{'authorId': 1, 'title': 1, 'contentType': 1, 'tags': 1, 'updatedAt': 1}"
    )
    Page<Article> findByAuthorId(Long userId, Pageable pageable);

    /**
     * 根据文章ID和作者ID查询文章是否存在
     *
     * @param articleId 文章ID
     * @param userId 作者ID
     * @return 是否存在
     */
    boolean existsByIdAndAuthorId(Long articleId, Long userId);
}
