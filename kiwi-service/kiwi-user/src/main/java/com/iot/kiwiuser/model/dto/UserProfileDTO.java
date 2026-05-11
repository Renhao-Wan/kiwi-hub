package com.iot.kiwiuser.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wan
 * 用户个人资料更新请求参数
 */
@Schema(description = "用户个人资料更新请求参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    @Schema(description = "用户简介", example = "热爱编程的开发者")
    private String bio;

    @Schema(description = "用户标签列表")
    private List<String> tags;
}
