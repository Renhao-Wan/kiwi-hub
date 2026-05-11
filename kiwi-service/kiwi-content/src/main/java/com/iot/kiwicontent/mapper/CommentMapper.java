package com.iot.kiwicontent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.kiwicontent.entity.CommentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论 Mapper 接口
 * 
 * <p>继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作。</p>
 * <p>对应 MySQL 表 `comment`。</p>
 * 
 * @author wan
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {
}