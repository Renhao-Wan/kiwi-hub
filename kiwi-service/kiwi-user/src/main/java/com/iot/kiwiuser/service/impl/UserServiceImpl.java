package com.iot.kiwiuser.service.impl;

import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwiuser.model.constant.ParameterConstant;
import com.iot.kiwiuser.model.constant.RabbitConstant;
import com.iot.kiwiuser.model.dto.UserProfileDTO;
import com.iot.kiwiuser.model.pojo.User;
import com.iot.kiwiuser.model.pojo.UserRelation;
import com.iot.kiwiuser.model.vo.UserCardVO;
import com.iot.kiwiuser.model.vo.UserDetailVO;
import com.iot.kiwiuser.repository.UserRelationRepository;
import com.iot.kiwiuser.repository.UserRepository;
import com.iot.kiwiuser.service.UserService;
import com.mongodb.MongoWriteException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRelationRepository userRelationRepository;
    private final MongoTemplate mongoTemplate;
    private final RabbitTemplate rabbitTemplate;

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
        boolean exists = userRepository.existsById(followUserId);
        if (!exists) {
            return Result.fail().message("关注的用户不存在");
        }
        // 保存关注关系
        UserRelation userRelation = new UserRelation(userId, followUserId);
        try {
            // 效率高且线程安全
            userRelationRepository.save(userRelation);
        } catch (Exception e) {
            // 捕获唯一索引冲突的异常
            if (e.getCause() instanceof MongoWriteException mongoWriteException) {
                if (mongoWriteException.getError().getCode() == 11000) {
                    return Result.fail().message("已关注该用户");
                }
            }
            throw e;
        }
        // RabbitMQ推送消息+更新 users 表的统计字段
        rabbitTemplate.convertAndSend(RabbitConstant.USER_RELATION_EXCHANGE,
                userId,
                Map.of(
                        ParameterConstant.FOLLOWER_ID, userId,
                        ParameterConstant.FOLLOWING_ID, followUserId,
                        ParameterConstant.FOLLOW_ACTION, ParameterConstant.FOLLOW
                ));
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
        // 直接删除，数据库自动处理不存在的情况
        userRelationRepository.deleteByFollowerIdAndFollowingId(userId, followUserId);
        rabbitTemplate.convertAndSend(RabbitConstant.USER_RELATION_EXCHANGE,
                userId,
                Map.of(
                        ParameterConstant.FOLLOWER_ID, userId,
                        ParameterConstant.FOLLOWING_ID, followUserId,
                        ParameterConstant.FOLLOW_ACTION, ParameterConstant.UNFOLLOW
                ));
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
        // 1. 设置分页，按时间倒序
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. 查询关系表：查“我关注了谁” (findByFollowerId)
        Page<UserRelation> relationPage = userRelationRepository.findByFollowerId(userId, pageable);

        // 3. 调用通用处理逻辑，传入提取规则：提取被关注人的ID (getFollowingId)
        return buildUserCardPage(relationPage, pageable, UserRelation::getFollowingId);
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
        // 1. 设置分页
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. 查询关系表：查“谁关注了我” (findByFollowingId)
        Page<UserRelation> relationPage = userRelationRepository.findByFollowingId(userId, pageable);

        // 3. 调用通用处理逻辑，传入提取规则：提取粉丝的ID (getFollowerId)
        return buildUserCardPage(relationPage, pageable, UserRelation::getFollowerId);
    }

    /**
     * 通用方法：将关系分页数据转换为用户卡片分页数据
     *
     * @param relationPage 查出来的关系分页对象
     * @param pageable     分页参数
     * @param idExtractor  函数式接口，定义如何从 UserRelation 中提取目标用户ID
     */
    private PageResult<UserCardVO> buildUserCardPage(Page<UserRelation> relationPage,
                                               Pageable pageable,
                                               Function<UserRelation, String> idExtractor) {
        if (relationPage.isEmpty()) {
            return PageResult.restPage(Page.empty());
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
        Page<UserCardVO> springPage = new PageImpl<>(voList, pageable, relationPage.getTotalElements());
        return PageResult.restPage(springPage);
    }
}
