package com.example.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    /*定义了一个名为 emailQueue 的 Spring Bean，该 Bean 是一个持久化的队列（Queue），名称为 mail*/

    @Bean("emailQueue")
    public Queue emailQueue() {
        return QueueBuilder.durable("mail").build();
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
