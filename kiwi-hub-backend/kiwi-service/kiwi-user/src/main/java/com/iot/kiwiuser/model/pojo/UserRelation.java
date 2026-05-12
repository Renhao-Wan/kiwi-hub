package com.iot.kiwiuser.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 用户关注关系
 * @author wan
 */
@Schema(description = "用户关注关系")
@Data
@Document(collection = "user_relations")
@CompoundIndexes({
        @CompoundIndex(name = "unique_relation", def = "{'follower_id': 1, 'following_id': 1}", unique = true),
        @CompoundIndex(name = "idx_follower_time", def = "{'follower_id': 1, 'created_at': -1}"),
        @CompoundIndex(name = "idx_following_time", def = "{'following_id': 1, 'created_at': -1}")
})
public class UserRelation {

    @Id
    @Schema(description = "关系ID")
    private String id;

    @Field("follower_id")
    @Schema(description = "发起关注的用户ID（粉丝）", required = true)
    private Long followerId;

    @Field("following_id")
    @Schema(description = "被关注的用户ID（目标）", required = true)
    private Long followingId;

    @Field("created_at")
    @Schema(description = "关注时间")
    private LocalDateTime createdAt;

    public UserRelation(Long followerId, Long followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
        this.createdAt = LocalDateTime.now();
    }
}
