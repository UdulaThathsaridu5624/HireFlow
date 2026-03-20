package com.ctse.profileservice.dto;

import java.util.List;

public class companyResponseDto {
    private Long id;
    private Long employeeId;
    private String companyName;
    private String location;
    private String industry;
    private String background;
    private String website;
    private Double reputationScore;
    private List<String> cultureTags;

    // Getters
    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getLocation() {
        return location;
    }

    public String getIndustry() {
        return industry;
    }

    public String getBackground() {
        return background;
    }

    public String getWebsite() {
        return website;
    }

    public Double getReputationScore() {
        return reputationScore;
    }

    public List<String> getCultureTags() {
        return cultureTags;
    }

    //setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setReputationScore(Double reputationScore) {
        this.reputationScore = reputationScore;
    }

    public void setCultureTags(List<String> cultureTags) {
        this.cultureTags = cultureTags;
    }
}
