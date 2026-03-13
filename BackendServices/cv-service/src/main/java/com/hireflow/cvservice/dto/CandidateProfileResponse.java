package com.hireflow.cvservice.dto;

import java.util.List;
import java.util.UUID;

public record CandidateProfileResponse(
        UUID candidateId,
        UUID userId,
        String bio,
        String location,
        String linkedinUrl,
        List<SkillResponse> skills,
        List<EducationResponse> education,
        List<WorkExperienceResponse> workExperience,
        List<ResumeResponse> resumes
) {
}
