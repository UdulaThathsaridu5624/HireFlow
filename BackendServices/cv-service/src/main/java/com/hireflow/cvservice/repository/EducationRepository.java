package com.hireflow.cvservice.repository;

import com.hireflow.cvservice.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EducationRepository extends JpaRepository<Education, UUID> {

    List<Education> findByCandidateId(UUID candidateId);

    void deleteAllByCandidateId(UUID candidateId);
}
