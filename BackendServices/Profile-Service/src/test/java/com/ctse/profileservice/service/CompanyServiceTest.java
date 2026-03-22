package com.ctse.profileservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.ctse.profileservice.dto.companyResponseDto;
import com.ctse.profileservice.dto.createCompanyDto;
import com.ctse.profileservice.entity.Company;
import com.ctse.profileservice.repository.companyRepository;
import com.ctse.profileservice.repository.culturalTagRepository;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private companyRepository companyRepository;

    @Mock
    private culturalTagRepository culturalTagRepository;

    @Mock
    private AzureBlobStorageService azureBlobStorageService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private companyFollowerService companyFollowerService;

    @InjectMocks
    private companyService companyService;

    private Company testCompany;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setEmployeeId("EMP123");
        testCompany.setCompanyName("Test Tech");
        testCompany.setIndustry("Software");
        testCompany.setLocation("New York");
    }

    @Test
    void getCompanyByEmployeeIdReturnsDtoWhenFound() {
        // Arrange
        String empId = "EMP123";
        String currentUserId = UUID.randomUUID().toString();
        when(companyRepository.findByEmployeeId(empId)).thenReturn(Optional.of(testCompany));
        when(companyFollowerService.getFollowersCount(1L)).thenReturn(100L);

        // Act
        Optional<companyResponseDto> result = companyService.getCompanyByEmployeeId(empId, currentUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("EMP123", result.get().getEmployeeId());
        assertEquals("Test Tech", result.get().getCompanyName());
        assertEquals(100L, result.get().getFollowersCount());
    }

    @Test
    void getCompanyByEmployeeIdReturnsEmptyWhenNotFound() {
        // Arrange
        String empId = "NON_EXISTENT";
        String currentUserId = UUID.randomUUID().toString();
        when(companyRepository.findByEmployeeId(empId)).thenReturn(Optional.empty());

        // Act
        Optional<companyResponseDto> result = companyService.getCompanyByEmployeeId(empId, currentUserId);

        // Assert
        assertFalse(result.isPresent());
    }
}
