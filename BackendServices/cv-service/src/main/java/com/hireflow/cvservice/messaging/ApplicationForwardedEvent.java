package com.hireflow.cvservice.messaging;

import com.hireflow.cvservice.model.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationForwardedEvent(
        UUID applicationId,
        UUID candidateId,
        UUID jobId,
        ApplicationStatus status,
        LocalDateTime forwardedAt
) {
}
