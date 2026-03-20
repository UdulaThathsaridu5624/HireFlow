package com.ctse.profileservice.service;

import org.springframework.stereotype.Service;

import com.ctse.profileservice.dto.createCompanyDto;
import com.ctse.profileservice.entity.Company;
import com.ctse.profileservice.entity.CompanyCultureTag;
import com.ctse.profileservice.repository.companyRepository;
import com.ctse.profileservice.repository.culturalTagRepository;

@Service
public class companyService {
    private final companyRepository companyRepository;
    private final culturalTagRepository culturalTagRepository;

    public companyService(companyRepository companyRepository, culturalTagRepository culturalTagRepository) {
        this.companyRepository = companyRepository;
        this.culturalTagRepository = culturalTagRepository;
    }

    public Company createCompany(createCompanyDto request, Long employeeId){
        Company company = new Company();
        company.setEmployeeId(request.getEmployeeId());
        company.setCompanyName(request.getCompanyName());
        company.setLocation(request.getLocation());
        company.setIndustry(request.getIndustry());
        company.setBackground(request.getBackground());
        company.setWebsite(request.getWebsite());
        company.setBackground(request.getBackground());

        Company savedCompany = companyRepository.save(company);

        //save cultural tags
        for(String tag : request.getCultureTags()) {
            CompanyCultureTag tags = new CompanyCultureTag();
            tags.setCompanyId(savedCompany.getId());
            tags.setCultureTagName(tag);
            culturalTagRepository.save(tags);
        }

        return savedCompany;
    }


}
