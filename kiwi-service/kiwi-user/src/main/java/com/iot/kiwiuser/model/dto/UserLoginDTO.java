package com.iot.kiwiuser.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author wan
 * 用户登录请求参数
 */
@Schema(description = "用户登录请求参数")
@Data
public class UserLoginDTO {

    @Schema(description = "用户ID")
    private String id;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", required = true, example = "john_doe")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^[\\w.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+$", message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", required = true, example = "john@example.com")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,12}$", message = "密码需包含字母和数字，长度6-12位")
    @Schema(description = "密码（需包含字母和数字，长度6-12位）", required = true, example = "abc123")
    private String password;
}
