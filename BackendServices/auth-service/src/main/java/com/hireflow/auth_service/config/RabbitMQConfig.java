package com.hireflow.auth_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String HIREFLOW_EXCHANGE      = "hireflow.exchange";
    public static final String HIREFLOW_EVENTS_QUEUE  = "hireflow_events_queue";
    public static final String AUTH_VALIDATE_QUEUE    = "auth_validate_queue";
    public static final String INTERVIEW_EVENTS_QUEUE = "interview_events_queue";

    @Bean
    public TopicExchange hireflowExchange() {
        return new TopicExchange(HIREFLOW_EXCHANGE);
    }

    @Bean
    public Queue hireflowEventsQueue() {
        return QueueBuilder.durable(HIREFLOW_EVENTS_QUEUE).build();
    }

    @Bean
    public Queue authValidateQueue() {
        return QueueBuilder.durable(AUTH_VALIDATE_QUEUE).build();
    }

    @Bean
    public Queue interviewEventsQueue() {
        return QueueBuilder.durable(INTERVIEW_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding hireflowEventsBinding() {
        return BindingBuilder.bind(hireflowEventsQueue()).to(hireflowExchange()).with("user.#");
    }

    @Bean
    public Binding authValidateBinding() {
        return BindingBuilder.bind(authValidateQueue()).to(hireflowExchange()).with("auth.validate");
    }

    @Bean
    public Binding interviewEventsBinding() {
        return BindingBuilder.bind(interviewEventsQueue()).to(hireflowExchange()).with("interview.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
