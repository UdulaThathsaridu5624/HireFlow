package com.ctse.profileservice.event;

public class ApplicationSubmittedEvent {

    private Long companyId;
    private Long jobId;
    private Long candidateId;

    //getters

    public Long getCompanyId() {
        return companyId;
    }

    public Long getJobId() {
        return jobId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    //setters

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }
}
