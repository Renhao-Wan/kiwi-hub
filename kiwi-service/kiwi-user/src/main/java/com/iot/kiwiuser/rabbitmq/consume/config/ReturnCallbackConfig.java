package com.iot.kiwiuser.rabbitmq.consume.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * @author wan
 * 监听消息发送失败
 */
@Slf4j
@Configuration
public class ReturnCallbackConfig  implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取RabbitTemplate
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        // 设置ReturnCallback
        rabbitTemplate.setReturnsCallback(returnMessage ->
                log.error("消息发送到队列失败！" +
                        " 应答码：{}" +
                        " 失败原因：{}" +
                        " 目标交换机：{}" +
                        " 目标路由键：{}" +
                        " 原始消息：{}",
                returnMessage.getReplyCode(),
                returnMessage.getReplyText(),
                returnMessage.getExchange(),
                returnMessage.getRoutingKey(),
                new String(returnMessage.getMessage().getBody(), StandardCharsets.UTF_8)
        ));
    }
}
