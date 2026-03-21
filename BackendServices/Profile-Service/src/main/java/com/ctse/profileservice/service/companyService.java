package com.ctse.profileservice.service;

import org.springframework.stereotype.Service;

import com.ctse.profileservice.dto.companyResponseDto;
import com.ctse.profileservice.dto.createCompanyDto;
import com.ctse.profileservice.entity.Company;
import com.ctse.profileservice.entity.CompanyCultureTag;
import com.ctse.profileservice.repository.companyRepository;
import com.ctse.profileservice.repository.culturalTagRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class companyService {
    private final companyRepository companyRepository;
    private final culturalTagRepository culturalTagRepository;
    private final AzureBlobStorageService azureBlobStorageService;

    public companyService(companyRepository companyRepository, culturalTagRepository culturalTagRepository,
            AzureBlobStorageService azureBlobStorageService) {
        this.companyRepository = companyRepository;
        this.culturalTagRepository = culturalTagRepository;
        this.azureBlobStorageService = azureBlobStorageService;
    }

    public List<companyResponseDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<companyResponseDto> getCompanyById(Long id) {
        return companyRepository.findById(id).map(this::mapToResponseDto);
    }

    public Optional<companyResponseDto> getCompanyByEmployeeId(String employeeId) {
        return companyRepository.findByEmployeeId(employeeId).map(this::mapToResponseDto);
    }

    public List<companyResponseDto> searchCompanies(String query) {
        return companyRepository.searchCompanies(query).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private companyResponseDto mapToResponseDto(Company company) {
        companyResponseDto dto = new companyResponseDto();
        dto.setId(company.getId());
        dto.setEmployeeId(company.getEmployeeId());
        dto.setCompanyName(company.getCompanyName());
        dto.setLocation(company.getLocation());
        dto.setIndustry(company.getIndustry());
        dto.setBackground(company.getBackground());
        dto.setWebsite(company.getWebsite());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setReputationScore(company.getReputationScore());

        List<String> tags = culturalTagRepository.findByCompanyId(company.getId()).stream()
                .map(CompanyCultureTag::getCultureTagName)
                .collect(Collectors.toList());
        dto.setCultureTags(tags);
        return dto;
    }

    public Company updateCompany(Long id, createCompanyDto request, String logoUrl) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        company.setCompanyName(request.getCompanyName());
        company.setLocation(request.getLocation());
        company.setIndustry(request.getIndustry());
        company.setBackground(request.getBackground());
        company.setWebsite(request.getWebsite());
        if (logoUrl != null) {
            company.setLogoUrl(logoUrl);
        }

        Company updatedCompany = companyRepository.save(company);

        // Update cultural tags (simple approach: delete and recreate)
        List<CompanyCultureTag> existingTags = culturalTagRepository.findByCompanyId(id);
        culturalTagRepository.deleteAll(existingTags);

        if (request.getCultureTags() != null) {
            for (String tag : request.getCultureTags()) {
                CompanyCultureTag newTag = new CompanyCultureTag();
                newTag.setCompanyId(id);
                newTag.setCultureTagName(tag);
                culturalTagRepository.save(newTag);
            }
        }

        return updatedCompany;
    }

    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        // delete tags first
        List<CompanyCultureTag> existingTags = culturalTagRepository.findByCompanyId(id);
        culturalTagRepository.deleteAll(existingTags);

        companyRepository.delete(company);
    }

    public Company createCompany(createCompanyDto request, String employeeId, String logoUrl) {
        Company company = new Company();
        company.setEmployeeId(request.getEmployeeId());
        company.setCompanyName(request.getCompanyName());
        company.setLocation(request.getLocation());
        company.setIndustry(request.getIndustry());
        company.setBackground(request.getBackground());
        company.setWebsite(request.getWebsite());
        company.setLogoUrl(logoUrl);

        Company savedCompany = companyRepository.save(company);

        // save cultural tags
        for (String tag : request.getCultureTags()) {
            CompanyCultureTag tags = new CompanyCultureTag();
            tags.setCompanyId(savedCompany.getId());
            tags.setCultureTagName(tag);
            culturalTagRepository.save(tags);
        }

        return savedCompany;
    }

}
