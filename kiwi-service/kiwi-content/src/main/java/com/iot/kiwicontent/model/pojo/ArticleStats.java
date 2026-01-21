package com.iot.kiwicontent.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 文章统计数据
 * @author wan
 */
@Schema(description = "文章统计数据")
@Data
public class ArticleStats {
    @Field("view_count")
    @Schema(description = "浏览量", example = "1250")
    private int viewCount = 0;

    @Field("like_count")
    @Schema(description = "点赞数", example = "156")
    private int likeCount = 0;

    @Field("comment_count")
    @Schema(description = "评论数", example = "12")
    private int commentCount = 0;
}
