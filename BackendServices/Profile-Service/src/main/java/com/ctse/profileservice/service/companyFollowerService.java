package com.ctse.profileservice.service;

import com.ctse.profileservice.entity.CompanyFollower;
import com.ctse.profileservice.repository.companyFollowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class companyFollowerService {

    private final companyFollowerRepository repository;
    private final EmployerAnalyticsService analyticsService;

    public void followCompany(Long companyId, UUID userId) {
        if (repository.findByCompanyIdAndUserId(companyId, userId).isEmpty()) {
            CompanyFollower follower = new CompanyFollower();
            follower.setCompanyId(companyId);
            follower.setUserId(userId);
            repository.save(follower);
            analyticsService.updateFollowersCount(companyId, getFollowersCount(companyId));
        }
    }

    public boolean isFollowing(Long companyId, UUID userId) {
        return repository.findByCompanyIdAndUserId(companyId, userId).isPresent();
    }

    public void unfollowCompany(Long companyId, UUID userId) {
        repository.findByCompanyIdAndUserId(companyId, userId).ifPresent(follower -> {
            repository.delete(follower);
            analyticsService.updateFollowersCount(companyId, getFollowersCount(companyId));
        });
    }

    public Long getFollowersCount(Long companyId) {
        return repository.countByCompanyId(companyId);
    }

    public List<Long> getFollowedCompanies(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(CompanyFollower::getCompanyId)
                .collect(Collectors.toList());
    }
}