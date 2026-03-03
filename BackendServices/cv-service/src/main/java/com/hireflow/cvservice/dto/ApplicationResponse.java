package com.hireflow.cvservice.dto;

import com.hireflow.cvservice.model.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationResponse(
        UUID applicationId,
        UUID candidateId,
        UUID jobId,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt,
        LocalDateTime forwardedAt
) {
}
