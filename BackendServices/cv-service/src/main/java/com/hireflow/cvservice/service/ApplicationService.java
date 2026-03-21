package com.hireflow.cvservice.service;

import com.hireflow.cvservice.client.JobListingClient;
import com.hireflow.cvservice.model.JobApplication;
import com.hireflow.cvservice.model.enums.ApplicationStatus;
import com.hireflow.cvservice.messaging.ApplicationForwardedEvent;
import com.hireflow.cvservice.messaging.ApplicationForwardingPublisher;
import com.hireflow.cvservice.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final CandidateService candidateService;
    private final JobListingClient jobListingClient;
    private final ApplicationForwardingPublisher forwardingPublisher;

    public ApplicationService(JobApplicationRepository applicationRepository,
                              CandidateService candidateService,
                              JobListingClient jobListingClient,
                              ApplicationForwardingPublisher forwardingPublisher) {
        this.applicationRepository = applicationRepository;
        this.candidateService = candidateService;
        this.jobListingClient = jobListingClient;
        this.forwardingPublisher = forwardingPublisher;
    }

    public JobApplication apply(UUID userId, UUID jobId) {

        UUID candidateId = candidateService.getCandidateEntityByUserId(userId).getId();

        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId)) {
            throw new IllegalArgumentException("You have already applied for this job");
        }

        jobListingClient.assertJobIsOpen(jobId);

        JobApplication application = new JobApplication();
        application.setCandidateId(candidateId);
        application.setJobId(jobId);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<JobApplication> getApplications(UUID userId) {

        UUID candidateId = candidateService.getCandidateEntityByUserId(userId).getId();
        return applicationRepository.findByCandidateId(candidateId);
    }

    public JobApplication withdrawApplication(UUID userId, UUID applicationId) {

        UUID candidateId = candidateService.getCandidateEntityByUserId(userId).getId();

        JobApplication application = applicationRepository
                .findByIdAndCandidateId(applicationId, candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() == ApplicationStatus.HIRED || application.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("This application can no longer be withdrawn");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setUpdatedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<JobApplication> getApplicationsForJob(UUID jobId) {

        return applicationRepository.findByJobId(jobId);
    }

    public JobApplication forwardToInterview(UUID applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        application.setStatus(ApplicationStatus.FORWARDED_TO_INTERVIEW);
        application.setForwardedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        JobApplication saved = applicationRepository.save(application);

        forwardingPublisher.publish(new ApplicationForwardedEvent(
                saved.getId(),
                saved.getCandidateId(),
                saved.getJobId(),
                saved.getStatus(),
                saved.getForwardedAt()
        ));

        return saved;
    }

    @Transactional(readOnly = true)
    public JobApplication getApplication(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
    }
}

