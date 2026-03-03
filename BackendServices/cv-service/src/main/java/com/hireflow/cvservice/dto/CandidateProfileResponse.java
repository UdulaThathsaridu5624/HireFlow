package com.hireflow.cvservice.dto;

import java.util.List;
import java.util.UUID;

public record CandidateProfileResponse(
        UUID candidateId,
        UUID userId,
        String bio,
        String location,
        List<SkillResponse> skills,
        List<EducationResponse> education,
        List<WorkExperienceResponse> workExperience
) {
}
