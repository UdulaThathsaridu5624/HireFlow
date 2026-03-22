package com.hireflow.cvservice.controller;

import com.hireflow.cvservice.dto.ApplicationResponse;
import com.hireflow.cvservice.model.JobApplication;
import com.hireflow.cvservice.model.enums.ApplicationStatus;
import com.hireflow.cvservice.service.ApplicationService;
import com.hireflow.cvservice.service.CandidateService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CandidateControllerTest {

    @Test
    void shouldReturnMyApplicationsForAuthenticatedCandidate() {
        CandidateService candidateService = new CandidateService(null, null, null, null, null, null);

        JobApplication application = new JobApplication();
        application.setId(UUID.randomUUID());
        application.setCandidateId(UUID.randomUUID());
        application.setJobId(UUID.randomUUID());
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        ApplicationService applicationService = new ApplicationService(null, null, null, null) {
            @Override
            public List<JobApplication> getApplications(UUID userId) {
                return List.of(application);
            }
        };

        CandidateController controller = new CandidateController(candidateService, applicationService, null);

        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(userId.toString())
                .build();

        List<ApplicationResponse> result = controller.myApplications(jwt);

        assertEquals(1, result.size());
        assertTrue(result.get(0).applicationId() != null);
        assertEquals(application.getJobId(), result.get(0).jobId());
    }
}
