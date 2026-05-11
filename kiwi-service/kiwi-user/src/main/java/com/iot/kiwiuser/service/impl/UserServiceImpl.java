package com.iot.kiwiuser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwiuser.entity.UserRelationEntity;
import com.iot.kiwiuser.mapper.UserRelationMapper;
import com.iot.kiwiuser.model.dto.UserProfileDTO;
import com.iot.kiwiuser.model.pojo.User;
import com.iot.kiwiuser.model.vo.UserCardVO;
import com.iot.kiwiuser.model.vo.UserDetailVO;
import com.iot.kiwiuser.repository.UserRepository;
import com.iot.kiwiuser.service.StatsService;
import com.iot.kiwiuser.service.UserEntityService;
import com.iot.kiwiuser.service.UserRelationEntityService;
import com.iot.kiwiuser.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * @author wan
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserRelationMapper, UserRelationEntity> implements UserService, UserRelationEntityService {

    private final UserRepository userRepository;
    private final UserEntityService userEntityService;
    private final MongoTemplate mongoTemplate;
    private final StatsService statsService;

    /**
     * 获取当前用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public UserDetailVO getCurrentUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseGet(() -> User.builder().build());
        UserDetailVO userDetailVO = new UserDetailVO();
        BeanUtils.copyProperties(user, userDetailVO);
        return userDetailVO;
    }

    /**
     * 关注用户
     * @param userId 用户ID
     * @param followUserId 关注用户ID
     * @return 是否成功
     */
    @Override
    public Result<Object> follow(Long userId, Long followUserId) {
        com.iot.kiwiuser.entity.UserEntity followUserEntity = userEntityService.getById(followUserId);
        if (followUserEntity == null) {
            return Result.fail().message("关注的用户不存在");
        }
        UserRelationEntity relationEntity = new UserRelationEntity();
        relationEntity.setFollowerId(userId);
        relationEntity.setFollowingId(followUserId);
        relationEntity.setCreatedAt(java.time.LocalDateTime.now());
        try {
            save(relationEntity);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return Result.fail().message("已关注该用户");
        }
        statsService.updateFollowStats(userId, followUserId, 1);
        return Result.success().message("关注成功");
    }

    /**
     * 取消关注用户
     * @param userId 用户ID
     * @param followUserId 取消关注用户ID
     * @return 是否成功
     */
    @Override
    public Result<Object> unfollow(Long userId, Long followUserId) {
        remove(
                new LambdaQueryWrapper<UserRelationEntity>()
                        .eq(UserRelationEntity::getFollowerId, userId)
                        .eq(UserRelationEntity::getFollowingId, followUserId)
        );
        statsService.updateFollowStats(userId, followUserId, -1);
        return Result.success().message("取消关注成功");
    }

    /**
     * 更新个人信息
     * @param userId 用户ID
     * @param profileDTO 个人信息
     */
    @Override
    public void updateProfile(Long userId, UserProfileDTO profileDTO) {
        Query query = new Query(Criteria.where("_id").is(userId));
        mongoTemplate.updateFirst(query,
                new Update()
                        .set("profile.bio", profileDTO.getBio())
                        .set("profile.tags", profileDTO.getTags()),
                User.class);
    }

    /**
     * 获取关注列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 关注列表
     */
    @Override
    public PageResult<UserCardVO> getFollowingList(Long userId, Integer pageNum, Integer pageSize) {
        Page<UserRelationEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelationEntity::getFollowerId, userId)
                .orderByDesc(UserRelationEntity::getCreatedAt);
        Page<UserRelationEntity> relationPage = this.page(page, wrapper);

        org.springframework.data.domain.Page<UserRelationEntity> springPage =
                new org.springframework.data.domain.PageImpl<>(
                        relationPage.getRecords(),
                        org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize),
                        relationPage.getTotal()
                );
        return buildUserCardPage(springPage, springPage.getPageable(), UserRelationEntity::getFollowingId);
    }

    /**
     * 获取粉丝列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 粉丝列表
     */
    @Override
    public PageResult<UserCardVO> getFollowersList(Long userId, Integer pageNum, Integer pageSize) {
        Page<UserRelationEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelationEntity::getFollowingId, userId)
                .orderByDesc(UserRelationEntity::getCreatedAt);
        Page<UserRelationEntity> relationPage = this.page(page, wrapper);

        org.springframework.data.domain.Page<UserRelationEntity> springPage =
                new org.springframework.data.domain.PageImpl<>(
                        relationPage.getRecords(),
                        org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize),
                        relationPage.getTotal()
                );
        return buildUserCardPage(springPage, springPage.getPageable(), UserRelationEntity::getFollowerId);
    }

    /**
     * 将关系分页数据转换为用户卡片分页数据
     */
    private PageResult<UserCardVO> buildUserCardPage(org.springframework.data.domain.Page<UserRelationEntity> relationPage,
                                                     org.springframework.data.domain.Pageable pageable,
                                                     Function<UserRelationEntity, Long> idExtractor) {
        if (relationPage.isEmpty()) {
            return PageResult.restPage(org.springframework.data.domain.Page.empty());
        }

        List<Long> targetIds = relationPage.getContent().stream()
                .map(idExtractor)
                .toList();

        List<User> users = userRepository.findAllById(targetIds);

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<UserCardVO> voList = relationPage.getContent().stream()
                .map(relation -> {
                    Long targetId = idExtractor.apply(relation);
                    User user = userMap.get(targetId);

                    UserCardVO vo = new UserCardVO();
                    if (user != null) {
                        BeanUtils.copyProperties(user, vo);
                        if (user.getProfile() != null) {
                            vo.setAvatarUrl(user.getProfile().getAvatarUrl());
                        }
                    } else {
                        vo.setUsername("用户已注销");
                    }
                    vo.setFollowTime(relation.getCreatedAt());
                    return vo;
                })
                .toList();
        org.springframework.data.domain.Page<UserCardVO> springPage =
                new org.springframework.data.domain.PageImpl<>(voList, pageable, relationPage.getTotalElements());
        return PageResult.restPage(springPage);
    }
}
