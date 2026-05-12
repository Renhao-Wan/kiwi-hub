package com.iot.kiwiuser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.kiwiuser.entity.UserRelationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户关注关系 Mapper 接口
 * 
 * <p>继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作。</p>
 * <p>对应 MySQL 表 `user_relation`。</p>
 * 
 * @author wan
 */
@Mapper
public interface UserRelationMapper extends BaseMapper<UserRelationEntity> {
}