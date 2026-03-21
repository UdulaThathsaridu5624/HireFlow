package com.ctse.profileservice.service;

import com.ctse.profileservice.entity.EmployerAnalytics;
import com.ctse.profileservice.repository.EmployerAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployerAnalyticsService {

    private final EmployerAnalyticsRepository analyticsRepository;
    private final companyService companyService;

    public EmployerAnalytics getAnalytics(Long companyId) {
        return analyticsRepository.findByCompanyId(companyId)
                .orElseGet(() -> analyticsRepository.save(
                        EmployerAnalytics.builder()
                                .companyId(companyId)
                                .profileViews(0L)
                                .jobPosts(0L)
                                .applicationsReceived(0L)
                                .followersCount(0L)
                                .build()));
    }

    @Transactional
    public void incrementProfileViews(Long companyId) {
        EmployerAnalytics analytics = getAnalytics(companyId);
        analytics.setProfileViews(analytics.getProfileViews() + 1);
        analyticsRepository.save(analytics);
        companyService.updateReputation(companyId);
    }

    @Transactional
    public void updateFollowersCount(Long companyId, Long count) {
        EmployerAnalytics analytics = getAnalytics(companyId);
        analytics.setFollowersCount(count);
        analyticsRepository.save(analytics);
        companyService.updateReputation(companyId);
    }

    @RabbitListener(queues = "job-post-events")
    @Transactional
    public void handleJobPostEvent(Long companyId) {
        EmployerAnalytics analytics = getAnalytics(companyId);
        analytics.setJobPosts(analytics.getJobPosts() + 1);
        analyticsRepository.save(analytics);
        companyService.updateReputation(companyId);
    }

    @RabbitListener(queues = "application-events")
    @Transactional
    public void handleApplicationEvent(Long companyId) {
        EmployerAnalytics analytics = getAnalytics(companyId);
        analytics.setApplicationsReceived(analytics.getApplicationsReceived() + 1);
        analyticsRepository.save(analytics);
        companyService.updateReputation(companyId);
    }
}
