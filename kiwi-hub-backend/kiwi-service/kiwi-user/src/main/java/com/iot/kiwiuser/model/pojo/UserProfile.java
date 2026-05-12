package com.iot.kiwiuser.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * 用户资料信息
 * @author wan
 */
@Schema(description = "用户资料信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Field("avatar_url")
    @Schema(description = "用户头像URL")
    private String avatarUrl;

    @Schema(description = "用户简介")
    private String bio;

    @Schema(description = "用户标签列表")
    private List<String> tags;
}
