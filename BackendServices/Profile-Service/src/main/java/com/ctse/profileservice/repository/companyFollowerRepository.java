package com.ctse.profileservice.repository;

import com.ctse.profileservice.entity.CompanyFollower;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface companyFollowerRepository extends JpaRepository<CompanyFollower, Long> {
    Optional<CompanyFollower> findByCompanyIdAndUserId(Long companyId, UUID userId);

    Long countByCompanyId(Long companyId);

    List<CompanyFollower> findByUserId(UUID userId);
}