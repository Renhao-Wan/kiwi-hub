package com.iot.kiwilink.service;

/**
 * 短链接服务接口
 * @author wan
 */
public interface LinkService {

    /**
     * 生成短链接服务
     * @param articleId 文章ID
     * @return 短链接
     */
    String generateShortLink(String articleId);

    /**
     * 获取长链接
     * 高速重定向。查询 Redis 映射。
     * @param code 短链接的Code
     * @return 完整的长连接
     */
    String getLongLink(String code);
}
