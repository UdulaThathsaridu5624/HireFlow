package com.hireflow.cvservice.repository;

import com.hireflow.cvservice.model.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, UUID> {

    List<CandidateSkill> findByCandidateId(UUID candidateId);

    void deleteAllByCandidateId(UUID candidateId);
}
