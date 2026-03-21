package com.hireflow.cvservice.controller;

import com.hireflow.cvservice.dto.ApplicationResponse;
import com.hireflow.cvservice.dto.CreateCandidateProfileRequest;
import com.hireflow.cvservice.dto.CandidateProfileResponse;
import com.hireflow.cvservice.dto.ApplyJobRequest;
import com.hireflow.cvservice.dto.ResumeResponse;
import com.hireflow.cvservice.service.CandidateService;
import com.hireflow.cvservice.service.ApplicationService;
import com.hireflow.cvservice.service.ResumeStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cv")
public class CandidateController {

    private final CandidateService candidateService;
    private final ApplicationService applicationService;
    private final ResumeStorageService resumeStorageService;

    public CandidateController(CandidateService candidateService,
                               ApplicationService applicationService,
                               ResumeStorageService resumeStorageService) {
        this.candidateService = candidateService;
        this.applicationService = applicationService;
        this.resumeStorageService = resumeStorageService;
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

    @PostMapping(value = "/resumes/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeResponse uploadResume(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isDefault", required = false, defaultValue = "false") Boolean isDefault) {
        UUID.fromString(jwt.getSubject()); // validates token subject format
        ResumeResponse stored = resumeStorageService.uploadPdf(file, isDefault);
        String absoluteFileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(stored.fileUrl())
            .toUriString();

        return new ResumeResponse(
            absoluteFileUrl,
            stored.fileName(),
            stored.isDefault(),
            stored.uploadedAt()
        );
    }

    @GetMapping("/resumes/files/{storedFileName}")
    public ResponseEntity<Resource> getResumeFile(@PathVariable String storedFileName) {
        Resource resource = resumeStorageService.loadAsResource(storedFileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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
