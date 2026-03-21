package com.hireflow.cvservice.dto;

public record SkillRequest(
        String name,
        String proficiencyLevel,
        Integer yearsExperience
) {
}
