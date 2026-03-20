package com.ctse.profileservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    // ─────────────────────────────────────────
    // COMPANY (existing)
    // ─────────────────────────────────────────
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
        return BindingBuilder.bind(analyticQueue)
                .to(companyExchange)
                .with("application.submitted");
    }

    // ─────────────────────────────────────────
    // AUTH SERVICE COMMUNICATION
    // ─────────────────────────────────────────
    public static final String HIREFLOW_EXCHANGE = "hireflow.exchange";

    @Bean
    public TopicExchange hireflowExchange() {
        return new TopicExchange(HIREFLOW_EXCHANGE);
    }

    // ─────────────────────────────────────────
    // 🔥 FIX: JSON Converter (CRITICAL)
    // ─────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // ✅ IGNORE __TypeId__ header from auth-service
        converter.setAlwaysConvertToInferredType(true);

        // ✅ Optional but safer: restrict type mapping
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();

        Map<String, Class<?>> idClassMapping = new HashMap<>();
        // Map incoming type to your local DTO
        idClassMapping.put(
                "com.hireflow.auth_service.dto.ValidateResponse",
                com.ctse.profileservice.dto.ValidateResponse.class);

        typeMapper.setIdClassMapping(idClassMapping);
        typeMapper.setTrustedPackages("*");

        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }

    // ─────────────────────────────────────────
    // RabbitTemplate (RPC)
    // ─────────────────────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        template.setMessageConverter(jsonMessageConverter());

        // ✅ Prevent hanging forever
        template.setReplyTimeout(5000);

        return template;
    }
}