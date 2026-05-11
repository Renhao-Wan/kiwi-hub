package com.iot.kiwiuser.repository;

import com.iot.kiwiuser.model.pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 用户数据访问接口
 * @author wan
 */
public interface UserRepository extends MongoRepository<User, Long> {

}
