package com.iot.kiwicontent.service.impl;

import com.iot.common.context.UserContext;
import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwicontent.client.UserServiceClient;
import com.iot.kiwicontent.model.dto.PublishArticleDTO;
import com.iot.kiwicontent.model.pojo.Article;
import com.iot.kiwicontent.model.pojo.ArticleStats;
import com.iot.kiwicontent.model.vo.ArticleListVO;
import com.iot.kiwicontent.repository.ArticleRepository;
import com.iot.kiwicontent.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wan
 * 文章服务实现类
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserServiceClient userServiceClient;

    /**
     * 发表文章
     *
     * @param userId 用户ID
     * @param publishArticleDTO 发表文章参数
     */
    @Override
    public void publishArticle(String userId, PublishArticleDTO publishArticleDTO) {
        Article article = Article.builder()
                .authorId(userId)
                .title(publishArticleDTO.getTitle())
                .content(publishArticleDTO.getContent())
                .stats(new ArticleStats())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        boolean hasText = StringUtils.hasText(publishArticleDTO.getContentType());
        String contentType = hasText ? publishArticleDTO.getContentType() : null;
        article.setContentType(contentType);
        List<String> ossUrls = CollectionUtils.isEmpty(publishArticleDTO.getOssUrls()) ? null : publishArticleDTO.getOssUrls();
        article.setOssUrls(ossUrls);
        List<String> tags = CollectionUtils.isEmpty(publishArticleDTO.getTags()) ? null : publishArticleDTO.getTags();
        article.setTags(tags);
        articleRepository.save(article);
        // 通知用户服务增加文章数 (使用 OpenFeign)
        userServiceClient.updateArticleCount(userId, 1);
    }

    /**
     * 删除文章
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 响应结果
     */
    @Override
    public Result<Object> deleteArticle(String userId, String articleId) {
        boolean exists = articleRepository.existsByIdAndAuthorId(articleId, userId);
        if (!exists) {
            return Result.fail().message("文章不存在或无权删除该文章");
        }
        articleRepository.deleteById(articleId);
        // 通知用户服务减少文章数 (使用 OpenFeign)
        userServiceClient.updateArticleCount(userId, -1);
        return Result.success();
    }

    /**
     * 获取文章列表
     *
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param currentUser 是否获取当前用户文章
     * @return 文章列表
     */
    @Override
    public PageResult<ArticleListVO> getArticleList(Integer pageNum, Integer pageSize, Boolean currentUser) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Article> page;
        if (currentUser) {
            String userId = UserContext.getUserId();
            page = articleRepository.findByAuthorId(userId, pageable);
        } else {
            page = articleRepository.findAll(pageable);
        }
        return PageResult.restPage(page.map(article -> new ArticleListVO(article.getId(),
                article.getTitle(),
                article.getContentType(),
                article.getTags(),
                article.getUpdatedAt())));
    }

    /**
     * 获取文章详情
     * @param articleId 文章ID
     * @return 文章详情
     */
    @Override
    public Article getArticleDetail(String articleId) {
        return articleRepository.findById(articleId)
                .orElse(Article.builder().build());
    }
}
