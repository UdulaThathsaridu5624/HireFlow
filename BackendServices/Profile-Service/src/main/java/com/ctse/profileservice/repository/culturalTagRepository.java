package com.ctse.profileservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ctse.profileservice.entity.CompanyCultureTag;

import java.util.List;

public interface culturalTagRepository extends JpaRepository<CompanyCultureTag,Long> {
    List <CompanyCultureTag> findByCompanyId(Long companyId);
}
