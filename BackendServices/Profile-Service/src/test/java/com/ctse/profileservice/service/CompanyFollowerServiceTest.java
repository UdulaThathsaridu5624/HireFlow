package com.ctse.profileservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ctse.profileservice.entity.CompanyFollower;
import com.ctse.profileservice.repository.companyFollowerRepository;

@ExtendWith(MockitoExtension.class)
class CompanyFollowerServiceTest {

    @Mock
    private companyFollowerRepository followerRepository;

    @Mock
    private EmployerAnalyticsService analyticsService;

    @InjectMocks
    private companyFollowerService followerService;

    @Test
    void followCompanyCreatesFollowerWhenNotExisting() {
        // Arrange
        Long companyId = 1L;
        UUID userId = UUID.randomUUID();

        when(followerRepository.findByCompanyIdAndUserId(companyId, userId)).thenReturn(Optional.empty());
        when(followerRepository.countByCompanyId(companyId)).thenReturn(5L);

        // Act
        followerService.followCompany(companyId, userId);

        // Assert
        verify(followerRepository).save(any(CompanyFollower.class));
        verify(analyticsService).updateFollowersCount(companyId, 5L);
    }

    @Test
    void followCompanyDoesNothingWhenAlreadyExisting() {
        // Arrange
        Long companyId = 1L;
        UUID userId = UUID.randomUUID();

        when(followerRepository.findByCompanyIdAndUserId(companyId, userId))
                .thenReturn(Optional.of(new CompanyFollower()));

        // Act
        followerService.followCompany(companyId, userId);

        // Assert
        verify(followerRepository, never()).save(any(CompanyFollower.class));
        verify(analyticsService, never()).updateFollowersCount(anyLong(), anyLong());
    }

    @Test
    void unfollowCompanyDeletesFollowerWhenExisting() {
        // Arrange
        Long companyId = 1L;
        UUID userId = UUID.randomUUID();
        CompanyFollower follower = new CompanyFollower();
        follower.setId(10L);

        when(followerRepository.findByCompanyIdAndUserId(companyId, userId)).thenReturn(Optional.of(follower));
        when(followerRepository.countByCompanyId(companyId)).thenReturn(4L);

        // Act
        followerService.unfollowCompany(companyId, userId);

        // Assert
        verify(followerRepository).delete(follower);
        verify(analyticsService).updateFollowersCount(companyId, 4L);
    }

    @Test
    void getFollowersCountReturnsCount() {
        // Arrange
        Long companyId = 1L;
        when(followerRepository.countByCompanyId(companyId)).thenReturn(42L);

        // Act
        Long count = followerService.getFollowersCount(companyId);

        // Assert
        assertEquals(42L, count);
    }
}
