package com.iot.kiwicontent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.kiwicontent.entity.ArticleStatsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章统计 Mapper 接口
 * 
 * <p>继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作。</p>
 * <p>对应 MySQL 表 `article_stats`。</p>
 * 
 * @author wan
 */
@Mapper
public interface ArticleStatsMapper extends BaseMapper<ArticleStatsEntity> {
}