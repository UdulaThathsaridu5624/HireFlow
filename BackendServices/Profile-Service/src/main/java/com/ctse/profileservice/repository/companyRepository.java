package com.ctse.profileservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ctse.profileservice.entity.Company;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface companyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByEmployeeId(String employeeId);

    @Query("SELECT DISTINCT c FROM Company c LEFT JOIN CompanyCultureTag t ON c.id = t.companyId " +
            "WHERE (:query IS NULL OR " +
            "      LOWER(c.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "      LOWER(c.industry) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "      LOWER(t.cultureTagName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Company> searchCompanies(@Param("query") String query);
}
