package com.iot.kiwicontent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.kiwicontent.mapper.CommentMapper;
import com.iot.common.exception.ServiceException;
import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwicontent.entity.CommentEntity;
import com.iot.kiwicontent.model.dto.CommentDTO;
import com.iot.kiwicontent.model.dto.CommentQueryDTO;
import com.iot.kiwicontent.model.vo.CommentVO;
import com.iot.kiwicontent.service.CommentService;
import com.iot.kiwicontent.service.ArticleStatsEntityService;
import com.iot.kiwicontent.service.CommentEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类（MySQL 存储）
 * 
 * @author wan
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentEntity> implements CommentService, CommentEntityService {


    private final ArticleStatsEntityService articleStatsEntityService;

    /**
     * 发表评论
     * @param userId 用户ID
     * @param commentDTO 评论DTO
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> publishComment(String userId, CommentDTO commentDTO) {
        LocalDateTime now = LocalDateTime.now();
        CommentEntity comment = new CommentEntity()
                .setArticleId(commentDTO.getArticleId())
                .setAuthorId(userId)
                .setContent(commentDTO.getContent())
                .setCreatedAt(now)
                .setStatus(0);

        if (commentDTO.getParentId() == null) {
            // 一级评论：插入后设置 root_id = id
            save(comment);
            comment.setRootId(comment.getId());
            updateById(comment);
        } else {
            // 子评论：验证父评论是否存在且未删除
            CommentEntity parent = getById(commentDTO.getParentId());
            if (parent == null || parent.getStatus() == 1) {
                throw new ServiceException("父评论不存在或已被删除");
            }
            comment.setParentId(parent.getId())
                    .setRootId(parent.getRootId());
            save(comment);
        }

        // 更新文章评论计数（+1）
        updateArticleCommentCount(commentDTO.getArticleId(), 1);

        log.info("评论发表成功，commentId: {}, articleId: {}, authorId: {}", comment.getId(), comment.getArticleId(), userId);
        return Result.success();
    }

    /**
     * 删除评论（软删除）
     * @param userId 用户ID
     * @param commentId 评论ID
     * @return Result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteComment(String userId, String commentId) {
        LambdaUpdateWrapper<CommentEntity> wrapper = new LambdaUpdateWrapper<CommentEntity>()
                .eq(CommentEntity::getId, commentId)
                .eq(CommentEntity::getAuthorId, userId)
                .eq(CommentEntity::getStatus, 0)
                .set(CommentEntity::getStatus, 1);

        boolean updated = update(wrapper);
        if (!updated) {
            return Result.fail().message("删除失败：评论不存在、已删除或无权操作");
        }

        // 获取评论所属文章ID，用于更新评论计数
        CommentEntity comment = getById(commentId);
        if (comment != null) {
            updateArticleCommentCount(comment.getArticleId(), -1);
        }

        log.info("评论删除成功，commentId: {}, authorId: {}", commentId, userId);
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
        LambdaQueryWrapper<CommentEntity> wrapper = new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getArticleId, articleId)
                .eq(CommentEntity::getStatus, 0)
                .apply("id = root_id")  // 一级评论：id 等于 root_id
                .orderByDesc(CommentEntity::getCreatedAt);

        Page<CommentEntity> page = new Page<>(pageNum, pageSize);
        page(page, wrapper);

        List<CommentVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), pageNum, pageSize, (int) page.getPages());
    }

    /**
     * 获取楼中楼回复列表（root_id = id，parent_id != id）
     * @param queryDTO 查询条件
     * @return Result<Map<String, Object>>
     */
    @Override
    public Result<Map<String, Object>> getReplies(CommentQueryDTO queryDTO) {
        LambdaQueryWrapper<CommentEntity> wrapper = new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getRootId, queryDTO.getRootId())
                .eq(CommentEntity::getStatus, 0)
                .ne(CommentEntity::getParentId, queryDTO.getRootId()) // 排除根评论自身
                .orderByAsc(CommentEntity::getId);

        if (queryDTO.getCursorId() != null && !queryDTO.getCursorId().isEmpty()) {
            wrapper.gt(CommentEntity::getId, queryDTO.getCursorId());
        }

        // 查询总数
        long total = count(wrapper);

        // 分页查询
        wrapper.last("LIMIT " + queryDTO.getPageSize());
        List<CommentEntity> comments = list(wrapper);

        List<CommentVO> commentVos = comments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 计算下一页的游标ID
        String nextCursorId = null;
        if (!comments.isEmpty()) {
            nextCursorId = comments.get(comments.size() - 1).getId();
        }

        Map<String, Object> result = new HashMap<>();
        if (nextCursorId != null) {
            result.put("nextCursorId", nextCursorId);
        }

        long totalPages = total % queryDTO.getPageSize() == 0 ? total / queryDTO.getPageSize() : total / queryDTO.getPageSize() + 1;
        PageResult<CommentVO> resultList = PageResult.of(commentVos, total, 1, queryDTO.getPageSize(), (int) totalPages);
        result.put("result", resultList);
        return Result.success(result);
    }

    /**
     * 将 CommentEntity 转换为 CommentVO
     */
    private CommentVO convertToVO(CommentEntity comment) {
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

    /**
     * 更新文章评论计数
     * @param articleId 文章ID
     * @param delta 变化量（+1 或 -1）
     */
    private void updateArticleCommentCount(String articleId, int delta) {
        try {
            articleStatsEntityService.updateCommentCount(articleId, delta);
        } catch (Exception e) {
            log.error("更新文章评论计数失败，articleId: {}, delta: {}", articleId, delta, e);
            // 不影响主流程，记录日志后继续
        }
    }
}