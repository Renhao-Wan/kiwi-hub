package com.iot.kiwiuser.service.impl;

import com.iot.common.result.Result;
import com.iot.kiwiuser.model.dto.UserLoginDTO;
import com.iot.kiwiuser.model.dto.UserRegisterDTO;
import com.iot.kiwiuser.model.pojo.User;
import com.iot.kiwiuser.model.pojo.UserProfile;
import com.iot.kiwiuser.model.pojo.UserStats;
import com.iot.kiwiuser.repository.UserRepository;
import com.iot.kiwiuser.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户认证服务实现类
 * @author wan
 */
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepository;
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
        boolean exists = userRepository.existsByUsernameOrEmail(username, email);
        if (exists) {
            return Result.fail().message("用户名或邮箱已存在");
        }

        String passwordHash = passwordEncoder.encode(registerDTO.getPassword());
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .socialStats(new UserStats())
                .createdAt(LocalDateTime.now())
                .build();
        String avatarUrl = null;
        if (avatarFile != null) {
            // 保存头像
            // TODO: 2023/10/23 保存头像
        }
        String bio = registerDTO.getBio().isBlank() ? null : registerDTO.getBio();
        List<String> tags = registerDTO.getTags().isEmpty() ? null : registerDTO.getTags();
        user.setProfile(new UserProfile(avatarUrl, bio, tags));
        userRepository.save(user);
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
        // 获取密码
        User user = userRepository.findPasswordByUsernameAndEmail(username, email)
                .orElseGet(() -> User.builder().build());
        String passwordHash = user.getPasswordHash();
        if (passwordHash == null || !passwordEncoder.matches(password, passwordHash)) {
            return Result.fail().message("昵称、邮箱或验证密码错误，请核对后重新输入");
        }
        loginDTO.setId(user.getId());
        return Result.success().message("登录成功");
    }

    /**
     * 注销账号
     * @param userId 用户 ID
     */
    @Override
    public void delete(String userId) {
        userRepository.deleteById(userId);
        // TODO: RabbitMQ 删除用户相关所有数据
    }
}
