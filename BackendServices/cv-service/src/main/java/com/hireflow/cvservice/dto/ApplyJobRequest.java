package com.hireflow.cvservice.dto;

import java.util.UUID;

public class ApplyJobRequest {

    private UUID jobId;

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }
}