package com.hireflow.cvservice.repository;

import com.hireflow.cvservice.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    List<Resume> findByCandidateId(UUID candidateId);

    void deleteAllByCandidateId(UUID candidateId);
}
