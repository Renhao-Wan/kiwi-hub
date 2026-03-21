package com.iot.kiwicontent.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 文章点赞记录
 * @author wan
 */
@Schema(description = "文章点赞记录")
@Data
@Document(collection = "article_likes")
@CompoundIndexes({
        @CompoundIndex(name = "idx_user_article_unique", def = "{'userId': 1, 'articleId': 1}", unique = true),
        @CompoundIndex(name = "idx_user_time", def = "{'userId': 1, 'createTime': -1}")
})
public class ArticleLike {

    @Id
    @Schema(description = "点赞记录ID")
    private String id;

    @Field("user_id")
    @Schema(description = "点赞用户ID", required = true)
    private Long userId;

    @Field("article_id")
    @Schema(description = "被点赞文章ID", required = true)
    private Long articleId;

    @Field("author_id")
    @Schema(description = "文章作者ID（冗余字段，方便快速统计）", required = true)
    private Long authorId;

    @Field("create_time")
    @Schema(description = "点赞时间")
    private LocalDateTime createTime;

    public ArticleLike(Long userId, Long articleId, Long authorId) {
        this.userId = userId;
        this.articleId = articleId;
        this.authorId = authorId;
        this.createTime = LocalDateTime.now();
    }
}
