package com.iot.kiwiuser.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 用户文档（MongoDB）
 * 存储用户半结构化资料数据和去规范化的用户名（用于快速查询）
 * @author wan
 */
@Schema(description = "用户文档")
@Data
@Builder
@CompoundIndex(name = "idx_username_unique", def = "{'username': 1}", unique = true)
@Document(collection = "users")
public class User {

    @Id
    @Schema(description = "用户ID（与MySQL user表id关联）")
    private Long id;

    @Schema(description = "用户名（去规范化存储，与MySQL同步）")
    private String username;

    @Schema(description = "用户资料信息")
    private UserProfile profile;
}