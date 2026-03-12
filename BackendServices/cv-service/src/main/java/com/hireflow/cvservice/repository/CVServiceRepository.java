package com.hireflow.cvservice.repository;

import com.hireflow.cvservice.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CVServiceRepository extends JpaRepository<Candidate, UUID> {
}
