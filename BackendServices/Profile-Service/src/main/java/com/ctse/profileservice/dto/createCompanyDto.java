package com.ctse.profileservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class createCompanyDto {
    private Long employeeId;
    private String companyName;
    private String location;
    private String industry;
    private String background;
    private String website;
    private List<String> cultureTags;

    //Getters

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

    public List<String> getCultureTags() {
        return cultureTags;
    }

    //Setters

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

    public void setCultureTags(List<String> cultureTags) {
        this.cultureTags = cultureTags;
    }
}

