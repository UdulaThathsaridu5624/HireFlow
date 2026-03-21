package com.hireflow.cvservice.repository;

import com.hireflow.cvservice.model.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkExperienceRepository extends JpaRepository<WorkExperience, UUID> {

    List<WorkExperience> findByCandidateId(UUID candidateId);

    void deleteAllByCandidateId(UUID candidateId);
}
