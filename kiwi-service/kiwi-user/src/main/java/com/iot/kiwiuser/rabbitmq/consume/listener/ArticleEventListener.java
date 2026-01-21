package com.iot.kiwiuser.rabbitmq.consume.listener;

import com.iot.kiwiuser.model.constant.ParameterConstant;
import com.iot.kiwiuser.model.constant.RabbitConstant;
import com.iot.kiwiuser.model.pojo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 文章服务监听器
 * @author wan
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleEventListener {
    private static final String ARTICLE_COUNT = "social_stats.article_count";

    private final MongoTemplate mongoTemplate;

    @RabbitListener(queues = RabbitConstant.ARTICLE_USER_QUEUE)
    public void handleArticleEvent(Map<String, String> message) {
        String authorId = message.get(ParameterConstant.AUTHOR_ID);
        String action = message.get(ParameterConstant.ACTION);
        switch (action) {
            case ParameterConstant.ARTICLE_PUBLISH:
                doUpdate(authorId, 1);
                break;
            case ParameterConstant.ARTICLE_DELETE:
                doUpdate(authorId, -1);
                break;
            default:
                log.error("Invalid action: {}", action);
        }
    }

    private void doUpdate(String authorId, int incCount) {
        Query query = Query.query(Criteria.where("_id").is(authorId));
        if (incCount < 0) {
            query.addCriteria(Criteria.where(ARTICLE_COUNT).gt(0));
        }
        Update update = new Update().inc(ARTICLE_COUNT, incCount);
        mongoTemplate.updateFirst(query, update, User.class);
        log.debug("更新用户文章数成功：authorId={}, incCount={}", authorId, incCount);
    }
}
