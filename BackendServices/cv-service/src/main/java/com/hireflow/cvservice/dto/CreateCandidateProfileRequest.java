package com.hireflow.cvservice.dto;

import java.util.List;

public class CreateCandidateProfileRequest {

    private String bio;
    private String location;
    private List<SkillRequest> skills;
    private List<EducationRequest> education;
    private List<WorkExperienceRequest> workExperience;

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<SkillRequest> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillRequest> skills) {
        this.skills = skills;
    }

    public List<EducationRequest> getEducation() {
        return education;
    }

    public void setEducation(List<EducationRequest> education) {
        this.education = education;
    }

    public List<WorkExperienceRequest> getWorkExperience() {
        return workExperience;
    }

    public void setWorkExperience(List<WorkExperienceRequest> workExperience) {
        this.workExperience = workExperience;
    }
}
