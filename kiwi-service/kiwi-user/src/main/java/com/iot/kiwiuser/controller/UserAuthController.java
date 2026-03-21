package com.iot.kiwiuser.controller;

import com.iot.common.constant.SessionConstant;
import com.iot.common.context.UserContext;
import com.iot.common.result.Result;
import com.iot.kiwiuser.model.dto.UserLoginDTO;
import com.iot.kiwiuser.model.dto.UserRegisterDTO;
import com.iot.kiwiuser.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证控制器
 * @author wan
 */
@Tag(name = "用户认证", description = "用户注册、登录、登出、注销等相关接口")
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    @Operation(summary = "用户注册", description = "新用户注册，支持上传头像")
    @PostMapping("/register")
    public Result<Object> register(@Parameter(description = "用户注册信息", required = true) @ModelAttribute @Validated UserRegisterDTO registerDTO,
                                   @Parameter(description = "用户头像文件") @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {
        return userAuthService.register(registerDTO, avatarFile);
    }

    @Operation(summary = "用户登录", description = "用户登录系统")
    @PostMapping("/login")
    public Result<Object> login(@Parameter(description = "用户登录信息", required = true) @RequestBody @Validated UserLoginDTO loginDTO, HttpSession session) {
        Result<Object> result = userAuthService.login(loginDTO);
        if (result.isSuccess()) {
            Map<String, Object> user = new HashMap<>();
            user.put(SessionConstant.ATTRIBUTE_USERNAME, loginDTO.getUsername());
            user.put(SessionConstant.ATTRIBUTE_ID, loginDTO.getId());
            session.setAttribute(SessionConstant.LOGIN_USER, user);
        }
        return result;
    }

    @Operation(summary = "用户登出", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<Object> logout(HttpSession session) {
        session.invalidate();
        return Result.success().message("登出成功");
    }

    @Operation(summary = "注销账号", description = "注销当前用户账号")
    @PostMapping("/delete")
    public Result<Object> delete(HttpSession session) {
        Long userId = UserContext.getUserId();
        userAuthService.delete(userId);
        session.invalidate();
        return Result.success().message("注销成功");
    }
}
