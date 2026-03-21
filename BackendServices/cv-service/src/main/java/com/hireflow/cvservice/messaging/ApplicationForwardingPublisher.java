package com.hireflow.cvservice.messaging;

import com.hireflow.cvservice.config.properties.MessagingProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApplicationForwardingPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final MessagingProperties messagingProperties;

	public ApplicationForwardingPublisher(RabbitTemplate rabbitTemplate,
										  MessagingProperties messagingProperties) {
		this.rabbitTemplate = rabbitTemplate;
		this.messagingProperties = messagingProperties;
	}

	public void publish(ApplicationForwardedEvent event) {
		String exchange = messagingProperties.getExchange();
		String routingKey = messagingProperties.getApplicationForwardedRoutingKey();

		if (exchange == null || exchange.isBlank()) {
			throw new IllegalStateException("Missing configuration: app.messaging.exchange");
		}
		if (routingKey == null || routingKey.isBlank()) {
			throw new IllegalStateException("Missing configuration: app.messaging.application-forwarded-routing-key");
		}

		try {
			rabbitTemplate.convertAndSend(
					exchange,
					routingKey,
					event
			);
		} catch (Exception e) {
			System.err.println("Failed to publish ApplicationForwardedEvent: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
