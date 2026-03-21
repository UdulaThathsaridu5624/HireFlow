package com.hireflow.cvservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.messaging")
public class MessagingProperties {

	private String exchange;
	private String applicationForwardedRoutingKey;

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getApplicationForwardedRoutingKey() {
		return applicationForwardedRoutingKey;
	}

	public void setApplicationForwardedRoutingKey(String applicationForwardedRoutingKey) {
		this.applicationForwardedRoutingKey = applicationForwardedRoutingKey;
	}
}
