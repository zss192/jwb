package com.jwb.search.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {
    /**
     * 交换机
     */
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("course.topic.exchange");
    }

    /**
     * 监听新增和修改的队列
     */
    @Bean
    public Queue insertQueue() {
        return new Queue("course.insert.queue", true);
    }

    /**
     * 监听删除的队列
     */
    @Bean
    public Queue deleteQueue() {
        return new Queue("course.delete.queue", true);
    }

    /**
     * 新增和修改的RoutingKey
     */
    @Bean
    public Binding insertQueueBinding() {
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with("course.insert");
    }

    /**
     * 删除的RoutingKey
     */
    @Bean
    public Binding deleteQueueBinding() {
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with("course.delete");
    }
}
