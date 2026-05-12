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
 * @author wan
 * 文章评论
 */
@Schema(description = "文章评论")
@Data
@CompoundIndexes({
        @CompoundIndex(name = "article_id_author_id_index", def = "{'article_id': 1, 'author_id': 1}"),
        @CompoundIndex(name = "idx_root_id", def = "{'root_id': 1}")
})
@Document(collection = "comments")
public class Comment {

    @Id
    @Schema(description = "评论ID")
    private String id;

    @Field("article_id")
    @Schema(description = "文章ID", required = true)
    private Long articleId;

    @Field("author_id")
    @Schema(description = "评论者ID", required = true)
    private Long authorId;

    @Schema(description = "评论内容", required = true)
    private String content;

    @Field("parent_id")
    @Schema(description = "父评论ID（用于实现盖楼回复结构）")
    private String parentId;

    @Field("root_id")
    @Schema(description = "根评论ID（用于快速定位评论串的根节点）")
    private String rootId;

    @Field("created_at")
    @Schema(description = "评论时间")
    private LocalDateTime createdAt;

    @Schema(description = "评论状态：0-正常, 1-已删除")
    private Integer status;
}
