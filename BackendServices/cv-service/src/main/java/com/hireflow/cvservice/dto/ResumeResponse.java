package com.hireflow.cvservice.dto;

import java.time.LocalDateTime;

public record ResumeResponse(
        String fileUrl,
        String fileName,
        Boolean isDefault,
        LocalDateTime uploadedAt
) {
}
