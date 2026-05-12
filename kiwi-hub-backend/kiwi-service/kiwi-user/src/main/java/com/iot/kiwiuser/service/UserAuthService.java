package com.iot.kiwiuser.service;

import com.iot.common.result.Result;
import com.iot.kiwiuser.model.dto.UserLoginDTO;
import com.iot.kiwiuser.model.dto.UserRegisterDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户认证服务接口
 * @author wan
 */
public interface UserAuthService {

    /**
     * 注册
     * @param registerDTO 注册信息
     * @param avatarFile 头像文件
     */
    Result<Object> register(UserRegisterDTO registerDTO, MultipartFile avatarFile);

    /**
     * 登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    Result<Object> login(UserLoginDTO loginDTO);

    /**
     * 注销账号
     * @param userId 用户 ID
     */
    void delete(Long userId);
}
