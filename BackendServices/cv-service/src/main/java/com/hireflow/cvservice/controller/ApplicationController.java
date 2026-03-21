package com.hireflow.cvservice.controller;

import com.hireflow.cvservice.dto.ApplicationResponse;
import com.hireflow.cvservice.dto.CandidateProfileResponse;
import com.hireflow.cvservice.service.ApplicationService;
import com.hireflow.cvservice.service.CandidateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employer")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final CandidateService candidateService;

    public ApplicationController(ApplicationService applicationService, CandidateService candidateService) {
        this.applicationService = applicationService;
        this.candidateService = candidateService;
    }

    @GetMapping("/jobs/{jobId}/applications")
    public List<ApplicationResponse> jobApplications(@PathVariable UUID jobId) {
        return applicationService.getApplicationsForJob(jobId).stream()
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

    @GetMapping("/applications/{applicationId}/cv")
    public CandidateProfileResponse candidateCvByApplication(@PathVariable UUID applicationId) {
        UUID candidateId = applicationService.getApplication(applicationId).getCandidateId();
        return candidateService.getCandidateProfile(candidateId);
    }

    @PostMapping("/applications/{applicationId}/forward")
    public ApplicationResponse forwardToInterview(@PathVariable UUID applicationId) {
        var application = applicationService.forwardToInterview(applicationId);
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
