package com.iot.kiwiuser.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iot.kiwiuser.model.pojo.UserProfile;
import com.iot.kiwiuser.model.pojo.UserStats;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户详情视图对象
 * @author wan
 */
@Schema(description = "用户详情视图对象")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailVO {
    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @Schema(description = "邮箱地址", example = "john@example.com")
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "注册时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "用户资料信息")
    private UserProfile profile;

    @Schema(description = "用户社交统计数据")
    private UserStats socialStats;
}
