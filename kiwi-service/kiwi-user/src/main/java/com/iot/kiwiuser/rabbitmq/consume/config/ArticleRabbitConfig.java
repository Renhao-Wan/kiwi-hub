package com.iot.kiwiuser.rabbitmq.consume.config;

import com.iot.kiwiuser.model.constant.RabbitConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文章服务 RabbitMQ 配置类
 * @author wan
 */
@Configuration
public class ArticleRabbitConfig {

    @Bean
    public Exchange articleUserExchange() {
        return ExchangeBuilder
                .directExchange(RabbitConstant.ARTICLE_USER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue articleUserQueue() {
        return QueueBuilder
                .durable(RabbitConstant.ARTICLE_USER_QUEUE)
                .build();
    }

    @Bean
    public Binding articleUserBinding(Queue articleUserQueue, Exchange articleUserExchange) {
        return BindingBuilder.bind(articleUserQueue)
                .to(articleUserExchange)
                .with("#")
                .noargs();
    }
}
