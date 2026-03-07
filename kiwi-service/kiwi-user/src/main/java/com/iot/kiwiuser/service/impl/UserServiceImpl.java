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
    public UserDetailVO getCurrentUserDetail(String userId) {
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
    public Result<Object> follow(String userId, String followUserId) {
        // 检查被关注用户是否存在（查询MySQL）
        com.iot.kiwiuser.entity.UserEntity followUserEntity = userEntityService.getById(followUserId);
        if (followUserEntity == null) {
            return Result.fail().message("关注的用户不存在");
        }
        // 保存关注关系到MySQL
        UserRelationEntity relationEntity = new UserRelationEntity();
        relationEntity.setFollowerId(userId);
        relationEntity.setFollowingId(followUserId);
        relationEntity.setCreatedAt(java.time.LocalDateTime.now());
        try {
            save(relationEntity);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return Result.fail().message("已关注该用户");
        }
        // 异步更新统计 (替代 RabbitMQ)
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
    public Result<Object> unfollow(String userId, String followUserId) {
        // 删除MySQL中的关注关系
        remove(
                new LambdaQueryWrapper<UserRelationEntity>()
                        .eq(UserRelationEntity::getFollowerId, userId)
                        .eq(UserRelationEntity::getFollowingId, followUserId)
        );
        // 异步更新统计 (替代 RabbitMQ)
        statsService.updateFollowStats(userId, followUserId, -1);
        return Result.success().message("取消关注成功");
    }

    /**
     * 更新个人信息
     * @param userId 用户ID
     * @param profileDTO 个人信息
     */
    @Override
    public void updateProfile(String userId, UserProfileDTO profileDTO) {
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
    public PageResult<UserCardVO> getFollowingList(String userId, Integer pageNum, Integer pageSize) {
        // 1. 使用 MyBatis-Plus 分页，按创建时间倒序
        Page<UserRelationEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelationEntity::getFollowerId, userId)
                .orderByDesc(UserRelationEntity::getCreatedAt);
        Page<UserRelationEntity> relationPage = this.page(page, wrapper);

        // 2. 转换为 Spring Data Page 格式（保持与原有方法兼容）
        org.springframework.data.domain.Page<UserRelationEntity> springPage =
                new org.springframework.data.domain.PageImpl<>(
                        relationPage.getRecords(),
                        org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize),
                        relationPage.getTotal()
                );

        // 3. 调用通用处理逻辑，传入提取规则：提取被关注人的ID (getFollowingId)
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
    public PageResult<UserCardVO> getFollowersList(String userId, Integer pageNum, Integer pageSize) {
        // 1. 使用 MyBatis-Plus 分页，按创建时间倒序
        Page<UserRelationEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelationEntity::getFollowingId, userId)
                .orderByDesc(UserRelationEntity::getCreatedAt);
        Page<UserRelationEntity> relationPage = this.page(page, wrapper);

        // 2. 转换为 Spring Data Page 格式（保持与原有方法兼容）
        org.springframework.data.domain.Page<UserRelationEntity> springPage =
                new org.springframework.data.domain.PageImpl<>(
                        relationPage.getRecords(),
                        org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize),
                        relationPage.getTotal()
                );

        // 3. 调用通用处理逻辑，传入提取规则：提取粉丝的ID (getFollowerId)
        return buildUserCardPage(springPage, springPage.getPageable(), UserRelationEntity::getFollowerId);
    }

    /**
     * 通用方法：将关系分页数据转换为用户卡片分页数据
     *
     * @param relationPage 查出来的关系分页对象
     * @param pageable     分页参数
     * @param idExtractor  函数式接口，定义如何从 UserRelation 中提取目标用户ID
     */
    private PageResult<UserCardVO> buildUserCardPage(org.springframework.data.domain.Page<UserRelationEntity> relationPage,
                                                org.springframework.data.domain.Pageable pageable,
                                                Function<UserRelationEntity, String> idExtractor) {
        if (relationPage.isEmpty()) {
            return PageResult.restPage(org.springframework.data.domain.Page.empty());
        }

        // 1. 利用传入的函数 idExtractor 提取目标 ID 列表
        List<String> targetIds = relationPage.getContent().stream()
                .map(idExtractor)
                .toList();

        // 2. 批量查询用户 TODO: 这里可以先查 Redis 缓存，未命中再查 DB
        List<User> users = userRepository.findAllById(targetIds);

        // 3. 转 Map 方便查找
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 4. 组装 VO
        List<UserCardVO> voList = relationPage.getContent().stream()
                .map(relation -> {
                    // 使用同样的提取规则获取当前记录对应的目标ID
                    String targetId = idExtractor.apply(relation);
                    User user = userMap.get(targetId);

                    UserCardVO vo = new UserCardVO();
                    // 必须判空，防止用户注销后 userMap 查不到导致 NPE
                    if (user != null) {
                        BeanUtils.copyProperties(user, vo);
                        // 假设 profile 不会为 null，否则这里也要判空
                        if (user.getProfile() != null) {
                            vo.setAvatarUrl(user.getProfile().getAvatarUrl());
                        }
                    } else {
                        // 处理用户已不存在的情况
                        vo.setUsername("用户已注销");
                    }

                    // 设置关注/粉丝关系的创建时间（这也是不联表的好处，时间保留在 relation 中）
                    vo.setFollowTime(relation.getCreatedAt());
                    return vo;
                })
                .toList();
        org.springframework.data.domain.Page<UserCardVO> springPage = new org.springframework.data.domain.PageImpl<>(voList, pageable, relationPage.getTotalElements());
        return PageResult.restPage(springPage);
    }
}
