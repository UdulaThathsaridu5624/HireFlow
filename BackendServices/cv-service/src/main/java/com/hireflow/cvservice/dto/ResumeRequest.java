package com.hireflow.cvservice.dto;

public record ResumeRequest(
        String fileUrl,
        String fileName,
        Boolean isDefault
) {
}
