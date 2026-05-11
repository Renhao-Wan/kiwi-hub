package com.iot.kiwiuser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.kiwiuser.entity.UserStatsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户统计 Mapper 接口
 * 
 * <p>继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作。</p>
 * <p>对应 MySQL 表 `user_stats`。</p>
 * 
 * @author wan
 */
@Mapper
public interface UserStatsMapper extends BaseMapper<UserStatsEntity> {
}