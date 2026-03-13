package com.hireflow.cvservice.service;

import com.hireflow.cvservice.dto.CreateCandidateProfileRequest;
import com.hireflow.cvservice.dto.CandidateProfileResponse;
import com.hireflow.cvservice.dto.EducationResponse;
import com.hireflow.cvservice.dto.ResumeResponse;
import com.hireflow.cvservice.dto.SkillResponse;
import com.hireflow.cvservice.dto.WorkExperienceResponse;
import com.hireflow.cvservice.model.Candidate;
import com.hireflow.cvservice.model.CandidateSkill;
import com.hireflow.cvservice.model.Education;
import com.hireflow.cvservice.model.Resume;
import com.hireflow.cvservice.model.Skill;
import com.hireflow.cvservice.model.WorkExperience;
import com.hireflow.cvservice.repository.CandidateSkillRepository;
import com.hireflow.cvservice.repository.CandidateRepository;
import com.hireflow.cvservice.repository.EducationRepository;
import com.hireflow.cvservice.repository.ResumeRepository;
import com.hireflow.cvservice.repository.SkillRepository;
import com.hireflow.cvservice.repository.WorkExperienceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final SkillRepository skillRepository;
    private final EducationRepository educationRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final ResumeRepository resumeRepository;

    public CandidateService(CandidateRepository candidateRepository,
                            CandidateSkillRepository candidateSkillRepository,
                            SkillRepository skillRepository,
                            EducationRepository educationRepository,
                            WorkExperienceRepository workExperienceRepository,
                            ResumeRepository resumeRepository) {
        this.candidateRepository = candidateRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.skillRepository = skillRepository;
        this.educationRepository = educationRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.resumeRepository = resumeRepository;
    }

    public CandidateProfileResponse upsertProfile(UUID userId, CreateCandidateProfileRequest request) {

        Candidate candidate = candidateRepository.findByUserId(userId).orElseGet(Candidate::new);

        candidate.setUserId(userId);
        candidate.setBio(request.getBio());
        candidate.setLocation(request.getLocation());
        candidate.setLinkedinUrl(request.getLinkedinUrl());

        candidate = candidateRepository.save(candidate);

        candidateSkillRepository.deleteAllByCandidateId(candidate.getId());
        educationRepository.deleteAllByCandidateId(candidate.getId());
        workExperienceRepository.deleteAllByCandidateId(candidate.getId());
        resumeRepository.deleteAllByCandidateId(candidate.getId());

        saveSkills(candidate, request);
        saveEducation(candidate, request);
        saveWorkExperience(candidate, request);
        saveResumes(candidate, request);

        return getCandidateProfile(candidate.getId());
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse getMyProfile(UUID userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found"));
        return getCandidateProfile(candidate.getId());
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse getCandidateProfile(UUID candidateId) {
        Candidate candidate = candidateRepository
                .findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        List<SkillResponse> skills = candidateSkillRepository.findByCandidateId(candidateId)
                .stream()
                .map(candidateSkill -> new SkillResponse(
                        candidateSkill.getSkill().getName(),
                        candidateSkill.getProficiencyLevel(),
                        candidateSkill.getYearsExperience()))
                .toList();

        List<EducationResponse> education = educationRepository.findByCandidateId(candidateId)
                .stream()
                .map(entry -> new EducationResponse(
                        entry.getInstitutionName(),
                        entry.getDegree(),
                        entry.getFieldOfStudy(),
                        entry.getStartDate(),
                        entry.getEndDate(),
                        entry.getGrade(),
                        entry.getDescription()))
                .toList();

        List<WorkExperienceResponse> workExperience = workExperienceRepository.findByCandidateId(candidateId)
                .stream()
                .map(entry -> new WorkExperienceResponse(
                        entry.getCompanyName(),
                        entry.getJobTitle(),
                        entry.getStartDate(),
                        entry.getEndDate(),
                        entry.getCurrentlyWorking(),
                        entry.getDescription()))
                .toList();

                List<ResumeResponse> resumes = resumeRepository.findByCandidateId(candidateId)
                    .stream()
                    .map(entry -> new ResumeResponse(
                        entry.getFileUrl(),
                        entry.getFileName(),
                        entry.getIsDefault(),
                        entry.getUploadedAt()))
                    .toList();

        return new CandidateProfileResponse(
                candidate.getId(),
                candidate.getUserId(),
                candidate.getBio(),
                candidate.getLocation(),
            candidate.getLinkedinUrl(),
                skills,
                education,
                workExperience,
                resumes);
    }

    @Transactional(readOnly = true)
    public Candidate getCandidateEntityByUserId(UUID userId) {
        return candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate profile not found"));
    }

    private void saveSkills(Candidate candidate, CreateCandidateProfileRequest request) {
        for (var skillRequest : safeList(request.getSkills())) {
            Skill skill = skillRepository.findByNameIgnoreCase(skillRequest.name())
                    .orElseGet(() -> {
                        Skill newSkill = new Skill();
                        newSkill.setName(skillRequest.name());
                        return skillRepository.save(newSkill);
                    });

            CandidateSkill candidateSkill = new CandidateSkill();
            candidateSkill.setCandidate(candidate);
            candidateSkill.setSkill(skill);
            candidateSkill.setProficiencyLevel(skillRequest.proficiencyLevel());
            candidateSkill.setYearsExperience(skillRequest.yearsExperience());
            candidateSkillRepository.save(candidateSkill);
        }
    }

    private void saveEducation(Candidate candidate, CreateCandidateProfileRequest request) {
        for (var educationRequest : safeList(request.getEducation())) {
            Education education = new Education();
            education.setCandidate(candidate);
            education.setInstitutionName(educationRequest.institutionName());
            education.setDegree(educationRequest.degree());
            education.setFieldOfStudy(educationRequest.fieldOfStudy());
            education.setStartDate(educationRequest.startDate());
            education.setEndDate(educationRequest.endDate());
            education.setGrade(educationRequest.grade());
            education.setDescription(educationRequest.description());
            educationRepository.save(education);
        }
    }

    private void saveWorkExperience(Candidate candidate, CreateCandidateProfileRequest request) {
        for (var workExperienceRequest : safeList(request.getWorkExperience())) {
            WorkExperience workExperience = new WorkExperience();
            workExperience.setCandidate(candidate);
            workExperience.setCompanyName(workExperienceRequest.companyName());
            workExperience.setJobTitle(workExperienceRequest.jobTitle());
            workExperience.setStartDate(workExperienceRequest.startDate());
            workExperience.setEndDate(workExperienceRequest.endDate());
            workExperience.setCurrentlyWorking(workExperienceRequest.currentlyWorking());
            workExperience.setDescription(workExperienceRequest.description());
            workExperienceRepository.save(workExperience);
        }
    }

    private void saveResumes(Candidate candidate, CreateCandidateProfileRequest request) {
        for (var resumeRequest : safeList(request.getResumes())) {
            Resume resume = new Resume();
            resume.setCandidateId(candidate.getId());
            resume.setFileUrl(resumeRequest.fileUrl());
            resume.setFileName(resumeRequest.fileName());
            resume.setIsDefault(Boolean.TRUE.equals(resumeRequest.isDefault()));
            resume.setUploadedAt(LocalDateTime.now());
            resumeRepository.save(resume);
        }
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }
}