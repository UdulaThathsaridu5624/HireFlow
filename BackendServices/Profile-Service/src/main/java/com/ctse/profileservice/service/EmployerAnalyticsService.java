package com.ctse.profileservice.service;

import com.ctse.profileservice.entity.EmployerAnalytics;
import com.ctse.profileservice.repository.EmployerAnalyticsRepository;
import com.ctse.profileservice.repository.companyRepository;
import com.ctse.profileservice.entity.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployerAnalyticsService {

    private final EmployerAnalyticsRepository analyticsRepository;
    private final companyRepository companyRepository;

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
        updateReputation(companyId);
    }

    @Transactional
    public void updateFollowersCount(Long companyId, Long count) {
        EmployerAnalytics analytics = getAnalytics(companyId);
        analytics.setFollowersCount(count);
        analyticsRepository.save(analytics);
        updateReputation(companyId);
    }

    @RabbitListener(queues = "job-post-events")
    @Transactional
    public void handleJobPostEvent(Long companyId) {
        EmployerAnalytics analytics = getAnalytics(companyId);
        analytics.setJobPosts(analytics.getJobPosts() + 1);
        analyticsRepository.save(analytics);
        updateReputation(companyId);
    }

    @RabbitListener(queues = "application-events")
    @Transactional
    public void handleApplicationEvent(Long companyId) {
        EmployerAnalytics analytics = getAnalytics(companyId);
        analytics.setApplicationsReceived(analytics.getApplicationsReceived() + 1);
        analyticsRepository.save(analytics);
        updateReputation(companyId);
    }

    @Transactional
    public void updateReputation(Long companyId) {
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company == null)
            return;

        analyticsRepository.findByCompanyId(companyId).ifPresent(analytics -> {
            double score = (analytics.getFollowersCount() * 10) +
                    (analytics.getJobPosts() * 5) +
                    (analytics.getApplicationsReceived() * 2) +
                    (analytics.getProfileViews() * 0.1);

            company.setReputationScore(score);
            companyRepository.save(company);
        });
    }
}
