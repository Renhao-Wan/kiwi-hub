package com.iot.kiwiuser.service;

import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwiuser.model.dto.UserProfileDTO;
import com.iot.kiwiuser.model.vo.UserCardVO;
import com.iot.kiwiuser.model.vo.UserDetailVO;

/**
 * 用户服务接口
 * @author wan
 */
public interface UserService {
    /**
     * 获取当前用户详情
     * @param userId 用户ID
     * @return 用户详情
     */
    UserDetailVO getCurrentUserDetail(Long userId);

    /**
     * 关注用户
     * @param userId 用户ID
     * @param followUserId 关注的用户ID
     * @return 响应结果
     */
    Result<Object> follow(Long userId, Long followUserId);

    /**
     * 取消关注用户
     * @param userId 用户ID
     * @param followUserId 取消关注的用户ID
     * @return 响应结果
     */
    Result<Object> unfollow(Long userId, Long followUserId);

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param profileDTO 用户信息
     */
    void updateProfile(Long userId, UserProfileDTO profileDTO);

    /**
     * 获取用户关注列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 关注列表
     */
    PageResult<UserCardVO> getFollowingList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户粉丝列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 粉丝列表
     */
    PageResult<UserCardVO> getFollowersList(Long userId, Integer pageNum, Integer pageSize);
}
