package com.iot.kiwiuser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.kiwiuser.mapper.UserMapper;
import com.iot.common.result.Result;
import com.iot.kiwiuser.entity.UserEntity;
import com.iot.kiwiuser.entity.UserStatsEntity;
import com.iot.kiwiuser.service.UserEntityService;
import com.iot.kiwiuser.service.UserStatsEntityService;
import com.iot.kiwiuser.model.dto.UserLoginDTO;
import com.iot.kiwiuser.model.dto.UserRegisterDTO;
import com.iot.kiwiuser.model.pojo.User;
import com.iot.kiwiuser.model.pojo.UserProfile;
import com.iot.kiwiuser.repository.UserRepository;
import com.iot.kiwiuser.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户认证服务实现类
 * @author wan
 */
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserAuthService, UserEntityService {

    private final UserRepository userRepository;
    private final UserStatsEntityService userStatsEntityService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 注册
     * @param registerDTO 注册信息
     * @param avatarFile 头像文件
     * @return 注册结果
     */
    @Override
    public Result<Object> register(UserRegisterDTO registerDTO, MultipartFile avatarFile) {
        // 查找是否已存在该用户，用户名或邮箱均不能重复
        String username = registerDTO.getUsername();
        String email = registerDTO.getEmail();
        
        // 检查MySQL中是否已存在相同用户名或邮箱
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getUsername, username).or().eq(UserEntity::getEmail, email);
        Long count = this.count(queryWrapper);
        if (count > 0) {
            return Result.fail().message("用户名或邮箱已存在");
        }

        // 1. 插入用户到MySQL
        String passwordHash = passwordEncoder.encode(registerDTO.getPassword());
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setEmail(email);
        userEntity.setPasswordHash(passwordHash);
        userEntity.setCreatedAt(java.time.LocalDateTime.now());
        this.save(userEntity);
        
        // 2. 插入用户统计数据到MySQL
        UserStatsEntity userStatsEntity = new UserStatsEntity();
        userStatsEntity.setUserId(userEntity.getId());
        userStatsEntity.setArticleCount(0);
        userStatsEntity.setFollowingCount(0);
        userStatsEntity.setFollowerCount(0);
        userStatsEntityService.save(userStatsEntity);
        
        // 3. 创建用户资料文档到MongoDB
        String avatarUrl = null;
        if (avatarFile != null) {
            // 保存头像
        }
        String bio = registerDTO.getBio() == null || registerDTO.getBio().isBlank() ? null : registerDTO.getBio();
        java.util.List<String> tags = registerDTO.getTags() == null || registerDTO.getTags().isEmpty() ? null : registerDTO.getTags();
        UserProfile profile = new UserProfile(avatarUrl, bio, tags);
        User userProfileDoc = User.builder()
                .id(userEntity.getId())
                .username(username)
                .profile(profile)
                .build();
        userRepository.save(userProfileDoc);
        
        return Result.success().message("注册成功");
    }

    /**
     * 登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @Override
    public Result<Object> login(UserLoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String email = loginDTO.getEmail();
        String password = loginDTO.getPassword();
        
        if ((username == null || username.isBlank()) && (email == null || email.isBlank())) {
            return Result.fail().message("用户名或邮箱不能为空");
        }
        
        // 查询MySQL用户表
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isBlank()) {
            queryWrapper.eq(UserEntity::getUsername, username);
        }
        if (email != null && !email.isBlank()) {
            queryWrapper.or().eq(UserEntity::getEmail, email);
        }
        UserEntity userEntity = this.getOne(queryWrapper);
        
        if (userEntity == null) {
            return Result.fail().message("昵称、邮箱或验证密码错误，请核对后重新输入");
        }
        
        String passwordHash = userEntity.getPasswordHash();
        if (passwordHash == null || !passwordEncoder.matches(password, passwordHash)) {
            return Result.fail().message("昵称、邮箱或验证密码错误，请核对后重新输入");
        }
        loginDTO.setId(userEntity.getId());
        return Result.success().message("登录成功");
    }

    /**
     * 注销账号
     * @param userId 用户 ID
     */
    @Override
    public void delete(Long userId) {
        // 1. 删除MySQL用户统计数据
        LambdaQueryWrapper<UserStatsEntity> statsWrapper = new LambdaQueryWrapper<>();
        statsWrapper.eq(UserStatsEntity::getUserId, userId);
        userStatsEntityService.remove(statsWrapper);
        
        // 2. 删除MySQL用户
        this.removeById(userId);
        
        // 3. 删除MongoDB用户资料文档
        userRepository.deleteById(userId);
    }
}
