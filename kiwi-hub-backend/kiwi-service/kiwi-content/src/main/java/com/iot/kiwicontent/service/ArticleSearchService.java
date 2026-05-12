package com.iot.kiwicontent.service;

import com.iot.common.result.PageResult;
import com.iot.kiwicontent.model.dto.ArticleSearchDTO;
import com.iot.kiwicontent.model.vo.ArticleSearchResultVO;

/**
 * 文章搜索服务接口
 * @author wan
 */
public interface ArticleSearchService {

    /**
     * 搜索文章
     *
     * @param searchDTO 搜索条件
     * @return 搜索结果
     */
    PageResult<ArticleSearchResultVO> searchArticles(ArticleSearchDTO searchDTO);

    /**
     * 获取热门搜索关键词
     *
     * @param limit 返回数量
     * @return 热门关键词列表
     */
    // List<String> getHotKeywords(int limit);

    /**
     * 获取搜索建议
     *
     * @param prefix 前缀
     * @param limit 返回数量
     * @return 搜索建议列表
     */
    // List<String> getSearchSuggestions(String prefix, int limit);
}