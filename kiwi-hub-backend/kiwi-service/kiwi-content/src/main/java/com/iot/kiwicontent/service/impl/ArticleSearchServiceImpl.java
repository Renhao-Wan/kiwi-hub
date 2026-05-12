package com.iot.kiwicontent.service.impl;

import com.iot.common.result.PageResult;
import com.iot.kiwicontent.entity.ArticleStatsEntity;
import com.iot.kiwicontent.model.dto.ArticleSearchDTO;
import com.iot.kiwicontent.model.pojo.ArticleContentDocument;
import com.iot.kiwicontent.model.vo.ArticleSearchResultVO;
import com.iot.kiwicontent.service.ArticleSearchService;
import com.iot.kiwicontent.service.ArticleStatsEntityService;
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
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文章搜索服务实现类（基于 MongoDB ArticleContentDocument 全文搜索）
 * 
 * @author wan
 */
@Service
@RequiredArgsConstructor
public class ArticleSearchServiceImpl implements ArticleSearchService {

    private final MongoTemplate mongoTemplate;
    private final ArticleStatsEntityService articleStatsEntityService;

    @Override
    public PageResult<ArticleSearchResultVO> searchArticles(ArticleSearchDTO searchDTO) {
        // 1. 使用 MongoDB 文本索引搜索 ArticleContentDocument
        List<ArticleContentDocument> contentDocs = searchByTextIndex(searchDTO);
        long total = countByTextIndex(searchDTO);

        // 2. 批量获取文章统计信息（从 MySQL）
        List<Long> articleIds = contentDocs.stream()
                .map(ArticleContentDocument::getArticleId)
                .collect(Collectors.toList());
        Map<Long, ArticleStatsEntity> statsMap = articleStatsEntityService.listByIds(articleIds)
                .stream()
                .collect(Collectors.toMap(ArticleStatsEntity::getArticleId, stats -> stats));

        // 3. 转换为 VO
        List<ArticleSearchResultVO> resultVos = contentDocs.stream()
                .map(doc -> convertToSearchResultVO(doc, statsMap.get(doc.getArticleId())))
                .toList();

        // 4. 添加搜索关键词高亮
        resultVos.forEach(vo -> addSearchHighlights(vo, searchDTO.getKeyword()));

        long totalPages = (total + searchDTO.getPageSize() - 1) / searchDTO.getPageSize();

        return PageResult.of(resultVos, total, searchDTO.getPageNum(), searchDTO.getPageSize(), Math.toIntExact(totalPages));
    }

    /**
     * 使用 MongoDB 文本索引搜索 ArticleContentDocument
     */
    private List<ArticleContentDocument> searchByTextIndex(ArticleSearchDTO searchDTO) {
        TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matching(searchDTO.getKeyword());
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

        return mongoTemplate.find(query, ArticleContentDocument.class);
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

        return mongoTemplate.count(query, ArticleContentDocument.class);
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
                // 注意：浏览量存储在 MySQL，无法在 MongoDB 排序，降级按时间排序
                return Sort.by(Sort.Direction.DESC, "updatedAt");
            case "likes":
                // 同理，降级按时间排序
                return Sort.by(Sort.Direction.DESC, "updatedAt");
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
     * 将 ArticleContentDocument 转换为 ArticleSearchResultVO
     */
    private ArticleSearchResultVO convertToSearchResultVO(ArticleContentDocument doc, ArticleStatsEntity stats) {
        ArticleSearchResultVO vo = new ArticleSearchResultVO();
        vo.setId(doc.getArticleId());
        vo.setAuthorId(doc.getAuthorId());
        vo.setTitle(doc.getTitle());

        // 生成摘要（截取前200个字符）
        String content = doc.getContent();
        if (content != null && content.length() > 200) {
            vo.setSummary(content.substring(0, 200) + "...");
        } else {
            vo.setSummary(content);
        }

        vo.setContentType(doc.getContentType());
        vo.setTags(doc.getTags());
        vo.setCreatedAt(doc.getCreatedAt());
        vo.setUpdatedAt(doc.getUpdatedAt());

        if (stats != null) {
            vo.setViewCount(stats.getViewCount());
            vo.setLikeCount(stats.getLikeCount());
            vo.setCommentCount(stats.getCommentCount());
        } else {
            vo.setViewCount(0);
            vo.setLikeCount(0);
            vo.setCommentCount(0);
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