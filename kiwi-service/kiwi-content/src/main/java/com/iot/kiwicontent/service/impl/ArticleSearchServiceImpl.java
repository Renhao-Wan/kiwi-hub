package com.iot.kiwicontent.service.impl;

import com.iot.common.result.PageResult;
import com.iot.kiwicontent.model.dto.ArticleSearchDTO;
import com.iot.kiwicontent.model.pojo.Article;
import com.iot.kiwicontent.model.vo.ArticleSearchResultVO;
import com.iot.kiwicontent.service.ArticleSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 文章搜索服务实现类
 * @author wan
 */
@Service
@RequiredArgsConstructor
public class ArticleSearchServiceImpl implements ArticleSearchService {

    private final MongoTemplate mongoTemplate;

    @Override
    public PageResult<ArticleSearchResultVO> searchArticles(ArticleSearchDTO searchDTO) {
        List<Article> articles;
        long total;

        // MongoDB 全文搜索
        articles = searchByTextIndex(searchDTO);
        total = countByTextIndex(searchDTO);

        // 转换为 VO
        List<ArticleSearchResultVO> resultVos = articles.stream()
                .map(this::convertToSearchResultVO)
                .toList();

        // 添加搜索关键词高亮
        resultVos.forEach(vo -> addSearchHighlights(vo, searchDTO.getKeyword()));

        long totalPages = (total + searchDTO.getPageSize() - 1) / searchDTO.getPageSize();

        return PageResult.of(resultVos, total, searchDTO.getPageNum(), searchDTO.getPageSize(), Math.toIntExact(totalPages));
    }

    /**
     * 使用 MongoDB 文本索引搜索
     */
    private List<Article> searchByTextIndex(ArticleSearchDTO searchDTO) {
        // 创建文本搜索条件
        TextCriteria textCriteria;
        textCriteria = TextCriteria.forDefaultLanguage().matching(searchDTO.getKeyword());

        // 创建查询
        TextQuery query = TextQuery.queryText(textCriteria);
        
        // 添加标签过滤
        if (searchDTO.getTags() != null && !searchDTO.getTags().isEmpty()) {
            String[] tagArray = searchDTO.getTags().split(",");
            query.addCriteria(Criteria.where("tags").in((Object[]) tagArray));
        }

        // 设置排序
        Sort sort = buildSort(searchDTO);
        query.with(sort);

        // 设置分页
        Pageable pageable = PageRequest.of(searchDTO.getPageNum() - 1, searchDTO.getPageSize());
        query.with(pageable);

        return mongoTemplate.find(query, Article.class);
    }

    /**
     * 统计文本索引搜索结果数量
     */
    private long countByTextIndex(ArticleSearchDTO searchDTO) {
        TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matching(searchDTO.getKeyword());
        TextQuery query = TextQuery.queryText(textCriteria);
        
        if (searchDTO.getTags() != null && !searchDTO.getTags().isEmpty()) {
            String[] tagArray = searchDTO.getTags().split(",");
            query.addCriteria(Criteria.where("tags").in((Object[]) tagArray));
        }

        return mongoTemplate.count(query, Article.class);
    }

    /**
     * 构建排序条件
     */
    private Sort buildSort(ArticleSearchDTO searchDTO) {
        Sort.Direction direction = "asc".equalsIgnoreCase(searchDTO.getSortOrder()) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        switch (searchDTO.getSortBy()) {
            case "time":
                return Sort.by(direction, "updatedAt");
            case "views":
                return Sort.by(direction, "stats.viewCount");
            case "likes":
                return Sort.by(direction, "stats.likeCount");
            case "relevance":
            default:
                // 全文搜索时按相关度排序，正则搜索时按时间排序
                if ("text".equalsIgnoreCase(searchDTO.getSearchMode())) {
                    return Sort.by(Sort.Direction.DESC, "score");
                } else {
                    return Sort.by(Sort.Direction.DESC, "updatedAt");
                }
        }
    }

    /**
     * 将 Article 转换为 ArticleSearchResultVO
     */
    private ArticleSearchResultVO convertToSearchResultVO(Article article) {
        ArticleSearchResultVO vo = new ArticleSearchResultVO();
        vo.setId(article.getId());
        vo.setAuthorId(article.getAuthorId());
        vo.setTitle(article.getTitle());
        
        // 生成摘要（截取前200个字符）
        String content = article.getContent();
        if (content != null && content.length() > 200) {
            vo.setSummary(content.substring(0, 200) + "...");
        } else {
            vo.setSummary(content);
        }
        
        vo.setContentType(article.getContentType());
        vo.setTags(article.getTags());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());
        
        if (article.getStats() != null) {
            vo.setViewCount(article.getStats().getViewCount());
            vo.setLikeCount(article.getStats().getLikeCount());
            vo.setCommentCount(article.getStats().getCommentCount());
        }
        return vo;
    }

    /**
     * 添加搜索关键词高亮
     */
    private void addSearchHighlights(ArticleSearchResultVO vo, String keyword) {
        List<String> highlights = new ArrayList<>();
        
        // 在标题中查找关键词
        if (vo.getTitle() != null && vo.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
            highlights.add("标题: " + highlightText(vo.getTitle(), keyword));
        }
        
        // 在摘要中查找关键词
        if (vo.getSummary() != null && vo.getSummary().toLowerCase().contains(keyword.toLowerCase())) {
            highlights.add("内容: " + highlightText(vo.getSummary(), keyword));
        }
        
        // 在标签中查找关键词
        if (vo.getTags() != null) {
            for (String tag : vo.getTags()) {
                if (tag.toLowerCase().contains(keyword.toLowerCase())) {
                    highlights.add("标签: " + highlightText(tag, keyword));
                }
            }
        }
        
        vo.setHighlights(highlights);
    }

    /**
     * 高亮显示关键词
     */
    private String highlightText(String text, String keyword) {
        if (text == null || keyword == null) {
            return text;
        }
        
        // 简单的高亮实现：用 ** 包裹关键词
        return text.replaceAll("(?i)" + Pattern.quote(keyword), "**$0**");
    }
}