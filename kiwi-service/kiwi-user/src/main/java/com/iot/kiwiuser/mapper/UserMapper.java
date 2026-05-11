package com.iot.kiwiuser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.kiwiuser.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户基础信息 Mapper 接口
 * 
 * <p>继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作。</p>
 * <p>对应 MySQL 表 `user`。</p>
 * 
 * @author wan
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}