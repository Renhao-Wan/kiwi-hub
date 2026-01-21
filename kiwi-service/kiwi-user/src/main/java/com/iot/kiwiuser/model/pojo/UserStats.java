package com.iot.kiwiuser.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 用户社交统计数据
 * @author wan
 */
@Schema(description = "用户社交统计数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {
    @Field("article_count")
    @Schema(description = "文章数", example = "25")
    private int articleCount = 0;

    @Field("following_count")
    @Schema(description = "关注数", example = "128")
    private int followingCount = 0;

    @Field("follower_count")
    @Schema(description = "粉丝数", example = "56")
    private int followerCount = 0;
}
