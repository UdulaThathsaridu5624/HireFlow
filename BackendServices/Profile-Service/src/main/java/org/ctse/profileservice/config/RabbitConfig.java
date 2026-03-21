package com.ctse.profileservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // ── Existing company exchange (keep as-is) ───────────────────────
    public static final String COMPANY_EXCHANGE = "company.exchange";
    public static final String ANALYTIC_QUEUE = "company.analytic.queue";

    @Bean
    public TopicExchange companyExchange() {
        return new TopicExchange(COMPANY_EXCHANGE);
    }

    @Bean
    public Queue analyticQueue() {
        return new Queue(ANALYTIC_QUEUE);
    }

    @Bean
    public Binding analyticBinding(Queue analyticQueue, TopicExchange companyExchange) {
        return BindingBuilder.bind(analyticQueue).to(companyExchange).with("application.submitted");
    }

    // ── New: connect to auth-service's hireflow.exchange ─────────────
    public static final String HIREFLOW_EXCHANGE = "hireflow.exchange";
    public static final String AUTH_VALIDATE_QUEUE = "auth_validate_queue";

    @Bean
    public TopicExchange hireflowExchange() {
        return new TopicExchange(HIREFLOW_EXCHANGE);
    }

    // ── JSON converter (required for RPC with auth-service) ──────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
