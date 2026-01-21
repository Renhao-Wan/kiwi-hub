package com.iot.kiwiuser.controller;

import com.iot.common.context.UserContext;
import com.iot.common.result.PageResult;
import com.iot.common.result.Result;
import com.iot.kiwiuser.model.dto.UserProfileDTO;
import com.iot.kiwiuser.model.vo.UserCardVO;
import com.iot.kiwiuser.model.vo.UserDetailVO;
import com.iot.kiwiuser.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * @author wan
 */
@Tag(name = "用户管理", description = "用户信息查询、关注、粉丝等相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserDetailVO> getCurrentUser() {
        String userId = UserContext.getUserId();
        return Result.success(userService.getCurrentUserDetail(userId));
    }

    @Operation(summary = "更新用户资料", description = "更新当前登录用户的个人资料")
    @PutMapping("/me/profile")
    public Result<Object> updateProfile(@Parameter(description = "用户资料信息", required = true) @ModelAttribute UserProfileDTO profileDTO) {
        String userId = UserContext.getUserId();
        userService.updateProfile(userId, profileDTO);
        return Result.success();
    }

    @Operation(summary = "关注用户", description = "关注指定用户")
    @PostMapping("/follow")
    public Result<Object> follow(@Parameter(description = "被关注用户的ID", required = true) @RequestParam String followUserId) {
        String userId = UserContext.getUserId();
        return userService.follow(userId, followUserId);
    }

    @Operation(summary = "取消关注", description = "取消关注指定用户")
    @DeleteMapping("/follow")
    public Result<Object> unfollow(@Parameter(description = "被取消关注用户的ID", required = true) @RequestParam String followUserId) {
        String userId = UserContext.getUserId();
        return userService.unfollow(userId, followUserId);
    }

    @Operation(summary = "获取关注列表", description = "分页获取当前用户的关注列表")
    @GetMapping("/following")
    public Result<PageResult<UserCardVO>> getFollowingList(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = UserContext.getUserId();
        return Result.success(userService.getFollowingList(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取粉丝列表", description = "分页获取当前用户的粉丝列表")
    @GetMapping("/followers")
    public Result<PageResult<UserCardVO>> getFollowersList(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                                           @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = UserContext.getUserId();
        return Result.success(userService.getFollowersList(userId, pageNum, pageSize));
    }
}
