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
    // COMPANY INTEGRATION (Job Service, etc.)
    // ─────────────────────────────────────────
    public static final String COMPANY_EXCHANGE = "company-exchange";
    public static final String COMPANY_CREATED_ROUTING_KEY = "company.created";
    public static final String ANALYTIC_QUEUE = "company.analytic.queue";
    public static final String JOB_SERVICE_COMPANY_QUEUE = "job-service-company-queue";
    public static final String JOB_POST_EVENTS_QUEUE = "job-post-events";
    public static final String APPLICATION_EVENTS_QUEUE = "application-events";

    @Bean
    public TopicExchange companyExchange() {
        return new TopicExchange(COMPANY_EXCHANGE);
    }

    @Bean
    public Queue analyticQueue() {
        return new Queue(ANALYTIC_QUEUE);
    }

    @Bean
    public Queue jobServiceQueue() {
        return new Queue(JOB_SERVICE_COMPANY_QUEUE, true);
    }

    @Bean
    public Queue jobPostEventsQueue() {
        return new Queue(JOB_POST_EVENTS_QUEUE, true);
    }

    @Bean
    public Queue applicationEventsQueue() {
        return new Queue(APPLICATION_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding analyticBinding(Queue analyticQueue, TopicExchange companyExchange) {
        return BindingBuilder.bind(analyticQueue)
                .to(companyExchange)
                .with("application.submitted");
    }

    @Bean
    public Binding jobServiceBinding(Queue jobServiceQueue, TopicExchange companyExchange) {
        return BindingBuilder.bind(jobServiceQueue)
                .to(companyExchange)
                .with(COMPANY_CREATED_ROUTING_KEY);
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