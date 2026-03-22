package com.ctse.profileservice.repository;

import com.ctse.profileservice.entity.EmployerAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployerAnalyticsRepository extends JpaRepository<EmployerAnalytics, Long> {
    Optional<EmployerAnalytics> findByCompanyId(Long companyId);
}
