package com.hireflow.cvservice.repository;

import com.hireflow.cvservice.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    List<JobApplication> findByCandidateId(UUID candidateId);

    List<JobApplication> findByJobId(UUID jobId);

    Optional<JobApplication> findByIdAndCandidateId(UUID id, UUID candidateId);

    boolean existsByCandidateIdAndJobId(UUID candidateId, UUID jobId);
}
