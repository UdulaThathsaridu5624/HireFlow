package com.hireflow.cvservice.dto;

import java.time.LocalDate;

public record WorkExperienceRequest(
        String companyName,
        String jobTitle,
        LocalDate startDate,
        LocalDate endDate,
        Boolean currentlyWorking,
        String description
) {
}
