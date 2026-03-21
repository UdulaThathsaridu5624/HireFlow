package com.ctse.profileservice.service;

import org.springframework.stereotype.Service;

import com.ctse.profileservice.dto.companyResponseDto;
import com.ctse.profileservice.dto.createCompanyDto;
import com.ctse.profileservice.entity.Company;
import com.ctse.profileservice.entity.CompanyCultureTag;
import com.ctse.profileservice.repository.companyRepository;
import com.ctse.profileservice.repository.culturalTagRepository;
import com.ctse.profileservice.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class companyService {
    private final companyRepository companyRepository;
    private final culturalTagRepository culturalTagRepository;
    private final AzureBlobStorageService azureBlobStorageService;
    private final RabbitTemplate rabbitTemplate;
    private final companyFollowerService companyFollowerService;

    public companyService(companyRepository companyRepository, culturalTagRepository culturalTagRepository,
            AzureBlobStorageService azureBlobStorageService, RabbitTemplate rabbitTemplate,
            companyFollowerService companyFollowerService) {
        this.companyRepository = companyRepository;
        this.culturalTagRepository = culturalTagRepository;
        this.azureBlobStorageService = azureBlobStorageService;
        this.rabbitTemplate = rabbitTemplate;
        this.companyFollowerService = companyFollowerService;
    }

    public Company createCompany(createCompanyDto request, String logoUrl) {
        Company company = new Company();
        company.setEmployeeId(request.getEmployeeId());
        company.setCompanyName(request.getCompanyName());
        company.setLocation(request.getLocation());
        company.setIndustry(request.getIndustry());
        company.setBackground(request.getBackground());
        company.setWebsite(request.getWebsite());
        company.setLogoUrl(logoUrl);

        Company savedCompany = companyRepository.save(company);

        // Add cultural tags
        if (request.getCultureTags() != null) {
            for (String tag : request.getCultureTags()) {
                CompanyCultureTag newTag = new CompanyCultureTag();
                newTag.setCompanyId(savedCompany.getId());
                newTag.setCultureTagName(tag);
                culturalTagRepository.save(newTag);
            }
        }

        // Notify Job Service about the new company ID
        rabbitTemplate.convertAndSend(RabbitConfig.COMPANY_EXCHANGE, RabbitConfig.COMPANY_CREATED_ROUTING_KEY,
                savedCompany.getId());

        return savedCompany;
    }

    public List<companyResponseDto> getAllCompanies(String currentUserId) {
        return companyRepository.findAll().stream()
                .map(c -> mapToResponseDto(c, currentUserId))
                .collect(Collectors.toList());
    }

    public Optional<companyResponseDto> getCompanyById(Long id, String currentUserId) {
        return companyRepository.findById(id).map(c -> mapToResponseDto(c, currentUserId));
    }

    public Optional<companyResponseDto> getCompanyByEmployeeId(String employeeId, String currentUserId) {
        return companyRepository.findByEmployeeId(employeeId).map(c -> mapToResponseDto(c, currentUserId));
    }

    public List<companyResponseDto> searchCompanies(String query, String currentUserId) {
        return companyRepository.searchCompanies(query).stream()
                .map(c -> mapToResponseDto(c, currentUserId))
                .collect(Collectors.toList());
    }

    private companyResponseDto mapToResponseDto(Company company, String currentUserId) {
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

        // Add follower stats
        dto.setFollowersCount(companyFollowerService.getFollowersCount(company.getId()));
        if (currentUserId != null && !currentUserId.isEmpty()) {
            dto.setFollowedByMe(companyFollowerService.isFollowing(company.getId(), UUID.fromString(currentUserId)));
        }

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
