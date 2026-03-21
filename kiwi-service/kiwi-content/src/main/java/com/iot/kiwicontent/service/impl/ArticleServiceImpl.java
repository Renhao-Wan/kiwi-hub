package com.iot.kiwicontent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.common.context.UserContext;
import com.iot.kiwicontent.entity.ArticleEntity;
import com.iot.kiwicontent.mapper.ArticleMapper;
import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwicontent.client.UserServiceClient;
import com.iot.kiwicontent.entity.ArticleEntity;
import com.iot.kiwicontent.entity.ArticleStatsEntity;
import com.iot.kiwicontent.model.dto.PublishArticleDTO;
import com.iot.kiwicontent.model.pojo.Article;
import com.iot.kiwicontent.model.pojo.ArticleContentDocument;
import com.iot.kiwicontent.model.pojo.ArticleStats;
import com.iot.kiwicontent.model.vo.ArticleListVO;
import com.iot.kiwicontent.repository.ArticleContentRepository;
import com.iot.kiwicontent.service.ArticleService;
import com.iot.kiwicontent.service.ArticleEntityService;
import com.iot.kiwicontent.service.ArticleStatsEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wan
 * 文章服务实现类（MySQL + MongoDB 混合存储）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, ArticleEntity> implements ArticleService, ArticleEntityService {

    private final ArticleStatsEntityService articleStatsEntityService;
    private final ArticleContentRepository articleContentRepository;
    private final UserServiceClient userServiceClient;

    /**
     * 发表文章（双写：MySQL 存储元数据，MongoDB 存储内容）
     *
     * @param userId 用户ID
     * @param publishArticleDTO 发表文章参数
     */
    @Override
    public void publishArticle(Long userId, PublishArticleDTO publishArticleDTO) {
        LocalDateTime now = LocalDateTime.now();
        Long articleId = null;
        try {
            ArticleEntity articleEntity = new ArticleEntity()
                    .setAuthorId(userId)
                    .setTitle(publishArticleDTO.getTitle())
                    .setCreatedAt(now)
                    .setUpdatedAt(now)
                    .setDeleted(0);
            save(articleEntity);
            articleId = articleEntity.getId();

            ArticleStatsEntity statsEntity = new ArticleStatsEntity()
                    .setArticleId(articleId)
                    .setViewCount(0)
                    .setLikeCount(0)
                    .setCommentCount(0)
                    .setCreatedAt(now)
                    .setUpdatedAt(now);
            articleStatsEntityService.save(statsEntity);

            ArticleContentDocument contentDoc = ArticleContentDocument.builder()
                    .articleId(articleId)
                    .authorId(userId)
                    .title(publishArticleDTO.getTitle())
                    .content(publishArticleDTO.getContent())
                    .contentType(StringUtils.hasText(publishArticleDTO.getContentType())
                            ? publishArticleDTO.getContentType()
                            : null)
                    .ossUrls(CollectionUtils.isEmpty(publishArticleDTO.getOssUrls())
                            ? null
                            : publishArticleDTO.getOssUrls())
                    .tags(CollectionUtils.isEmpty(publishArticleDTO.getTags())
                            ? null
                            : publishArticleDTO.getTags())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            articleContentRepository.save(contentDoc);

            userServiceClient.updateArticleCount(userId, 1);

            log.info("文章发布成功，articleId: {}, authorId: {}", articleId, userId);
        } catch (Exception e) {
            log.error("文章发布失败，已执行补偿操作，articleId: {}, authorId: {}", articleId, userId, e);
            if (articleId != null) {
                try {
                    removeById(articleId);
                    articleStatsEntityService.remove(new LambdaQueryWrapper<ArticleStatsEntity>()
                            .eq(ArticleStatsEntity::getArticleId, articleId));
                    articleContentRepository.deleteByArticleId(articleId);
                } catch (Exception ex) {
                    log.error("补偿删除失败，articleId: {}", articleId, ex);
                }
            }
            throw new RuntimeException("文章发布失败，请重试");
        }
    }

    /**
     * 删除文章（同时清理 MySQL 和 MongoDB）
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 响应结果
     */
    @Override
    public Result<Object> deleteArticle(Long userId, Long articleId) {
        ArticleEntity articleEntity = getById(articleId);
        if (articleEntity == null || !userId.equals(articleEntity.getAuthorId())) {
            return Result.fail().message("文章不存在或无权删除该文章");
        }

        try {
            articleEntity.setDeleted(1);
            updateById(articleEntity);

            articleStatsEntityService.remove(new LambdaQueryWrapper<ArticleStatsEntity>()
                    .eq(ArticleStatsEntity::getArticleId, articleId));

            articleContentRepository.deleteByArticleId(articleId);

            userServiceClient.updateArticleCount(userId, -1);

            log.info("文章删除成功，articleId: {}, authorId: {}", articleId, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("文章删除失败，articleId: {}, authorId: {}", articleId, userId, e);
            return Result.fail().message("删除失败，请重试");
        }
    }

    /**
     * 获取文章列表（仅元数据，来自 MySQL）
     *
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param currentUser 是否获取当前用户文章
     * @return 文章列表
     */
    @Override
    public PageResult<ArticleListVO> getArticleList(Integer pageNum, Integer pageSize, Boolean currentUser) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));

        LambdaQueryWrapper<ArticleEntity> wrapper = new LambdaQueryWrapper<ArticleEntity>()
                .eq(ArticleEntity::getDeleted, 0)
                .orderByDesc(ArticleEntity::getUpdatedAt);

        if (currentUser) {
            Long userId = UserContext.getUserId();
            wrapper.eq(ArticleEntity::getAuthorId, userId);
        }

        // 执行分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ArticleEntity> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        page(mpPage, wrapper);

        // 转换为 ArticleListVO
        List<ArticleListVO> voList = mpPage.getRecords().stream()
                .map(entity -> new ArticleListVO(
                        entity.getId(),
                        entity.getTitle(),
                        null,
                        null,
                        entity.getUpdatedAt()))
                .collect(Collectors.toList());

        // 构建 Spring Data Page 对象以保持兼容
        Page<ArticleListVO> page = new PageImpl<>(voList, pageable, mpPage.getTotal());
        return PageResult.restPage(page);
    }

    /**
     * 获取文章详情（组合 MySQL 元数据 + MongoDB 内容 + MySQL 统计）
     *
     * @param articleId 文章ID
     * @return 文章详情
     */
    @Override
    public Article getArticleDetail(Long articleId) {
        ArticleEntity entity = getById(articleId);
        if (entity == null || entity.getDeleted() == 1) {
            return Article.builder().build();
        }

        ArticleContentDocument contentDoc = articleContentRepository.findByArticleId(articleId);
        if (contentDoc == null) {
            log.warn("文章内容不存在，articleId: {}", articleId);
            return Article.builder().build();
        }

        ArticleStatsEntity statsEntity = articleStatsEntityService.getOne(new LambdaQueryWrapper<ArticleStatsEntity>()
                .eq(ArticleStatsEntity::getArticleId, articleId));
        ArticleStats stats = new ArticleStats();
        if (statsEntity != null) {
            stats.setViewCount(statsEntity.getViewCount());
            stats.setLikeCount(statsEntity.getLikeCount());
            stats.setCommentCount(statsEntity.getCommentCount());
        }

        return Article.builder()
                .id(entity.getId())
                .authorId(entity.getAuthorId())
                .title(entity.getTitle())
                .content(contentDoc.getContent())
                .contentType(contentDoc.getContentType())
                .ossUrls(contentDoc.getOssUrls())
                .tags(contentDoc.getTags())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .stats(stats)
                .build();
    }
}