package com.iot.kiwicontent.service.impl;

import com.iot.common.exception.ServiceException;
import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwicontent.model.dto.CommentDTO;
import com.iot.kiwicontent.model.dto.CommentQueryDTO;
import com.iot.kiwicontent.model.pojo.Comment;
import com.iot.kiwicontent.model.vo.CommentVO;
import com.iot.kiwicontent.repository.CommentRepository;
import com.iot.kiwicontent.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 * @author wan
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * 发表评论
     * @param userId 用户ID
     * @param commentDTO 评论DTO
     * @return Result
     */
    @Override
    public Result<Object> publishComment(String userId, CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setArticleId(commentDTO.getArticleId());
        comment.setAuthorId(userId);
        comment.setContent(commentDTO.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        // 正常状态
        comment.setStatus(0);
        String newId = new ObjectId().toHexString();
        comment.setId(newId);

        if (commentDTO.getParentId() == null) {
            // 一级评论
            comment.setParentId(null);
            comment.setRootId(newId);
        } else {
            //即使父评论删了，子评论展示时可以显示“回复 @已删除用户”。这不算数据损坏，只是业务逻辑的边缘情况。
            // 子评论
            Comment parent = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new ServiceException("父评论不存在"));
            
            if (parent.getStatus() == 1) {
                throw new ServiceException("父评论已被删除");
            }
            
            comment.setParentId(parent.getId());
            comment.setRootId(parent.getRootId());
        }
        commentRepository.save(comment);

        // TODO: 发送MQ消息触发未读消息通知、更新文章评论计数（Redis缓存）
        
        return Result.success();
    }

    /**
     * 删除评论
     * @param userId 用户ID
     * @param commentId 评论ID
     * @return Result
     */
    @Override
    public Result<Object> deleteComment(String userId, String commentId) {
        // 构建查询条件：ID匹配 且 作者匹配 且 尚未删除
        Query query = new Query(Criteria.where("_id").is(commentId)
                .and("authorId").is(userId)
                .and("status").is(0));

        // 设置更新操作
        Update update = new Update().set("status", 1);

        // updateFirst 是原子操作，返回更新的结果，保证状态流转的原子性
        // 只要这一条语句执行成功，就不用担心并发问题
        var updateResult = mongoTemplate.updateFirst(query, update, Comment.class);

        if (updateResult.getModifiedCount() == 0) {
            return Result.fail().message("删除失败：评论不存在、已删除或无权操作");
        }

        // TODO: 这里可能需要发消息去减少文章的评论计数（最终一致性）
        return Result.success();
    }

    /**
     * 获取文章的一级评论列表（root_id = id）
     * @param articleId 文章ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return PageResult<CommentVO>
     */
    @Override
    public PageResult<CommentVO> getRootComments(String articleId, Integer pageNum, Integer pageSize) {
        // 使用 MongoTemplate 实现复杂查询：rootId = id
        Query query = new Query();
        query.addCriteria(Criteria.where("articleId").is(articleId)
                .and("status").is(0)
                .andOperator(Criteria.where("$expr").is(
                        new org.bson.Document("$eq", java.util.Arrays.asList("$rootId", "$_id"))
                )));
        
        // 计算总数
        long total = mongoTemplate.count(query, Comment.class);
        
        // 分页查询
        query.with(PageRequest.of(pageNum - 1, pageSize));
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<Comment> rootComments = mongoTemplate.find(query, Comment.class);
        
        List<CommentVO> commentVos = rootComments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        
        return PageResult.of(commentVos, total, pageNum, pageSize, Math.toIntExact(totalPages));
    }

    /**
     * 获取楼中楼回复列表（root_id = id，parent_id != id）
     * @param queryDTO 查询条件
     * @return PageResult<CommentVO>
     */
    @Override
    public Result<Map<String, Object>> getReplies(CommentQueryDTO queryDTO) {
        List<Comment> comments;
        long total;

        List<Comment> allComments;
        if (queryDTO.getCursorId() == null || queryDTO.getCursorId().isEmpty()) {
            // 第一页：查询所有符合条件的评论，然后手动分页
            allComments = commentRepository.findByRootIdAndStatusOrderById(queryDTO.getRootId(), 0);
            total = allComments.size();
        } else {
            // 游标分页：查询ID大于游标ID的评论
            allComments = commentRepository.findByRootIdAndIdGreaterThanAndStatusOrderById(queryDTO.getRootId(), queryDTO.getCursorId(), 0);
            total = commentRepository.countByRootId(queryDTO.getRootId());
        }
        comments = allComments.stream()
                .limit(queryDTO.getPageSize())
                .toList();

        List<CommentVO> commentVos = comments.stream()
                .map(this::convertToVO)
                .toList();
        
        // 计算下一页的游标ID
        String nextCursorId;
        Map<String, Object> result = new HashMap<>();
        if (!comments.isEmpty()) {
            nextCursorId = comments.get(comments.size() - 1).getId();
            result.put("nextCursorId", nextCursorId);
        }

        long totalPages = total % queryDTO.getPageSize() == 0 ? total / queryDTO.getPageSize() : total / queryDTO.getPageSize() + 1;
        
        PageResult<CommentVO> resultList = PageResult.of(commentVos, total, 1, queryDTO.getPageSize(), Math.toIntExact(totalPages));
        result.put("result", resultList);
        return Result.success(result);
    }

    /**
     * 将Comment实体转换为CommentVO
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setAuthorId(comment.getAuthorId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setRootId(comment.getRootId());
        vo.setStatus(comment.getStatus());
        vo.setCreatedAt(comment.getCreatedAt());
        
        // 如果评论已删除，替换内容
        if (comment.getStatus() == 1) {
            vo.setContent("该评论已删除");
            vo.setAuthorName("已删除用户");
            vo.setAuthorAvatar("");
        }
        
        return vo;
    }
}