package com.hireflow.cvservice.client;

import com.hireflow.cvservice.config.properties.JobServiceProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class JobListingClient {

	private final JobServiceProperties jobServiceProperties;

	public JobListingClient(JobServiceProperties jobServiceProperties) {
		this.jobServiceProperties = jobServiceProperties;
	}

	public void assertJobIsOpen(UUID jobId) {
		if (!jobServiceProperties.isEnabled()) {
			return;
		}

		if (jobServiceProperties.getBaseUrl() == null || jobServiceProperties.getBaseUrl().isBlank()
				|| jobServiceProperties.getOpenPath() == null || jobServiceProperties.getOpenPath().isBlank()) {
			throw new IllegalStateException("Job service is enabled but not configured. Set app.job-service.base-url and app.job-service.open-path");
		}

		Object responseBody = RestClient.builder()
				.baseUrl(jobServiceProperties.getBaseUrl())
				.build()
				.get()
				.uri(jobServiceProperties.getOpenPath(), jobId)
				.retrieve()
				.body(Object.class);

		boolean isOpen = extractOpenFlag(responseBody);
		if (!isOpen) {
			throw new IllegalArgumentException("The selected job is not open for applications");
		}
	}

	private boolean extractOpenFlag(Object responseBody) {
		if (responseBody instanceof Boolean value) {
			return value;
		}
		if (responseBody instanceof Map<?, ?> map) {
			Object open = map.get("open");
			if (open == null) {
				open = map.get("isOpen");
			}
			return Boolean.TRUE.equals(open);
		}
		return false;
	}
}
