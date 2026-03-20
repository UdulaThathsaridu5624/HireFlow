package com.ctse.profileservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ctse.profileservice.entity.Company;

public interface companyRepository extends JpaRepository<Company, Long> {
}
