package com.iot.kiwicontent.repository;

import com.iot.kiwicontent.model.pojo.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * 评论仓库接口
 * @author wan
 */
public interface CommentRepository extends MongoRepository<Comment, String> {

    /**
     * 查询文章的一级评论列表（root_id = id）
     *
     * @param articleId 文章ID
     * @param pageable 分页参数
     * @return 一级评论列表
     */
    Page<Comment> findByArticleIdAndRootIdAndStatus(Long articleId, Long rootId, Integer status, Pageable pageable);

    /**
     * 查询指定根评论下的所有回复（游标分页）
     *
     * @param rootId 根评论ID
     * @param cursorId 游标ID（大于此ID）
     * @param status 状态
     * @return 回复列表
     */
    List<Comment> findByRootIdAndIdGreaterThanAndStatusOrderById(String rootId, String cursorId, Integer status);

    /**
     * 查询指定根评论下的所有回复（第一页）
     *
     * @param rootId 根评论ID
     * @param status 状态
     * @return 回复列表
     */
    List<Comment> findByRootIdAndStatusOrderById(String rootId, Integer status);

    /**
     * 根据文章ID和作者ID查询评论是否存在
     *
     * @param articleId 文章ID
     * @param authorId 作者ID
     * @return 是否存在
     */
    boolean existsByArticleIdAndAuthorId(Long articleId, Long authorId);

    /**
     * 根据ID和作者ID查询评论
     *
     * @param id 评论ID
     * @param authorId 作者ID
     * @return 评论
     */
    Comment findByIdAndAuthorId(String id, Long authorId);

    /**
     * 统计文章的评论数量
     *
     * @param articleId 文章ID
     * @return 评论数量
     */
    long countByArticleId(Long articleId);

    /**
     * 统计根评论下的回复数量
     *
     * @param rootId 根评论ID
     * @return 回复数量
     */
    long countByRootId(String rootId);
}