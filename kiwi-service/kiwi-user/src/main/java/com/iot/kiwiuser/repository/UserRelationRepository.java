package com.iot.kiwiuser.repository;

import com.iot.kiwiuser.model.pojo.UserRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 用户关系存储接口
 * @author wan
 */
public interface UserRelationRepository extends MongoRepository<UserRelation, String> {

    /**
     * 删除指定用户关系
     * @param followerId 关注者 ID
     * @param followingId 被关注者 ID
     */
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * 根据关注者 ID 查询用户关系
     * @param followerId 关注者 ID
     * @param pageable 分页参数
     * @return 用户关系列表
     */
    Page<UserRelation> findByFollowerId(Long followerId, Pageable pageable);

    /**
     * 根据被关注者 ID 获取用户关系
     * @param followingId 被关注者 ID
     * @param pageable 分页参数
     * @return 用户关系列表
     */
    Page<UserRelation> findByFollowingId(Long followingId, Pageable pageable);
}
