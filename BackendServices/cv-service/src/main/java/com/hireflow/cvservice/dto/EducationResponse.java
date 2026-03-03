package com.hireflow.cvservice.dto;

import java.time.LocalDate;

public record EducationResponse(
        String institutionName,
        String degree,
        String fieldOfStudy,
        LocalDate startDate,
        LocalDate endDate,
        String grade,
        String description
) {
}
