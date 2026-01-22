package com.iot.kiwicontent.service;

import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwicontent.model.dto.CommentDTO;
import com.iot.kiwicontent.model.dto.CommentQueryDTO;
import com.iot.kiwicontent.model.vo.CommentVO;

import java.util.Map;

/**
 * 评论服务接口
 * @author wan
 */
public interface CommentService {

    /**
     * 发布评论
     *
     * @param userId 用户ID
     * @param commentDTO 评论DTO
     * @return 操作结果
     */
    Result<Object> publishComment(String userId, CommentDTO commentDTO);

    /**
     * 删除评论（软删除）
     *
     * @param userId 用户ID
     * @param commentId 评论ID
     * @return 操作结果
     */
    Result<Object> deleteComment(String userId, String commentId);

    /**
     * 查询文章的一级评论列表
     *
     * @param articleId 文章ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 一级评论列表
     */
    PageResult<CommentVO> getRootComments(String articleId, Integer pageNum, Integer pageSize);

    /**
     * 查询楼中楼回复列表（游标分页）
     *
     * @param queryDTO 查询条件
     * @return 回复列表
     */
    Result<Map<String, Object>> getReplies(CommentQueryDTO queryDTO);
}