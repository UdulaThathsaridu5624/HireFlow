package com.ctse.profileservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ctse.profileservice.entity.Company;
import com.ctse.profileservice.entity.EmployerAnalytics;
import com.ctse.profileservice.repository.EmployerAnalyticsRepository;
import com.ctse.profileservice.repository.companyRepository;

@ExtendWith(MockitoExtension.class)
class EmployerAnalyticsServiceTest {

    @Mock
    private EmployerAnalyticsRepository analyticsRepository;

    @Mock
    private companyRepository companyRepository;

    @Mock
    private companyFollowerService followerService;

    @InjectMocks
    private EmployerAnalyticsService analyticsService;

    @Test
    void getAnalyticsReturnsExisting() {
        // Arrange
        Long companyId = 1L;
        EmployerAnalytics existingAnalytics = new EmployerAnalytics();
        existingAnalytics.setId(10L);
        existingAnalytics.setCompanyId(companyId);

        when(analyticsRepository.findByCompanyId(companyId)).thenReturn(Optional.of(existingAnalytics));

        // Act
        EmployerAnalytics result = analyticsService.getAnalytics(companyId);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(analyticsRepository, never()).save(any());
    }

    @Test
    void getAnalyticsCreatesNewWhenNotExists() {
        // Arrange
        Long companyId = 1L;
        when(analyticsRepository.findByCompanyId(companyId)).thenReturn(Optional.empty());

        EmployerAnalytics savedAnalytics = new EmployerAnalytics();
        savedAnalytics.setCompanyId(companyId);

        when(analyticsRepository.save(any(EmployerAnalytics.class))).thenReturn(savedAnalytics);

        // Act
        EmployerAnalytics result = analyticsService.getAnalytics(companyId);

        // Assert
        assertNotNull(result);
        assertEquals(companyId, result.getCompanyId());
        verify(analyticsRepository).save(any(EmployerAnalytics.class));
    }

    @Test
    void incrementProfileViewsSuccess() {
        // Arrange
        Long companyId = 1L;
        EmployerAnalytics analytics = new EmployerAnalytics();
        analytics.setCompanyId(companyId);
        analytics.setProfileViews(5L);

        when(analyticsRepository.findByCompanyId(companyId)).thenReturn(Optional.of(analytics));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(new Company()));

        // Act
        analyticsService.incrementProfileViews(companyId);

        // Assert
        assertEquals(6L, analytics.getProfileViews());
        verify(analyticsRepository).save(analytics);
        verify(companyRepository).save(any(Company.class)); // verification of updateReputation trigger
    }
}
