package com.hireflow.cvservice.controller;

import com.hireflow.cvservice.dto.ApplicationResponse;
import com.hireflow.cvservice.dto.CreateCandidateProfileRequest;
import com.hireflow.cvservice.dto.CandidateProfileResponse;
import com.hireflow.cvservice.dto.ApplyJobRequest;
import com.hireflow.cvservice.service.CandidateService;
import com.hireflow.cvservice.service.ApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cv")
public class CandidateController {

    private final CandidateService candidateService;
    private final ApplicationService applicationService;

    public CandidateController(CandidateService candidateService, ApplicationService applicationService) {
        this.candidateService = candidateService;
        this.applicationService = applicationService;
    }

    @PutMapping("/profile")
    public CandidateProfileResponse upsertProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateCandidateProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return candidateService.upsertProfile(userId, request);
    }

    @GetMapping("/profile")
    public CandidateProfileResponse getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return candidateService.getMyProfile(userId);
    }

    @PostMapping("/applications")
    public ApplicationResponse apply(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ApplyJobRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var application = applicationService.apply(userId, request.getJobId());
        return new ApplicationResponse(
                application.getId(),
                application.getCandidateId(),
                application.getJobId(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getUpdatedAt(),
                application.getForwardedAt());
    }

    @GetMapping("/applications")
    public List<ApplicationResponse> myApplications(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return applicationService.getApplications(userId).stream()
                .map(application -> new ApplicationResponse(
                        application.getId(),
                        application.getCandidateId(),
                        application.getJobId(),
                        application.getStatus(),
                        application.getAppliedAt(),
                        application.getUpdatedAt(),
                        application.getForwardedAt()))
                .toList();
    }

    @PatchMapping("/applications/{id}/withdraw")
    public ApplicationResponse withdraw(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var application = applicationService.withdrawApplication(userId, id);
        return new ApplicationResponse(
                application.getId(),
                application.getCandidateId(),
                application.getJobId(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getUpdatedAt(),
                application.getForwardedAt());
    }
}
