package com.hireflow.cvservice.config;

import com.hireflow.cvservice.config.properties.MessagingProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    TopicExchange cvExchange(MessagingProperties messagingProperties) {
        return new TopicExchange(messagingProperties.getExchange(), true, false);
    }
}
