package com.ctse.profileservice.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ctse.profileservice.dto.ValidateResponse;
import com.ctse.profileservice.dto.createCompanyDto;
import com.ctse.profileservice.entity.Company;
import com.ctse.profileservice.service.AuthValidationService;
import com.ctse.profileservice.service.AzureBlobStorageService;
import com.ctse.profileservice.service.companyService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class companyController {

    private final companyService companyService;
    private final AuthValidationService authValidationService;
    private final AzureBlobStorageService azureBlobStorageService;

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<?> createCompany(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestPart("company") createCompanyDto request,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {

        // 1. Check Authorization header exists and has Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // strip "Bearer "

        // 2. Validate token via auth-service RabbitMQ RPC
        ValidateResponse auth = authValidationService.validate(token);

        if (!auth.isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }

        // 3. Enforce EMPLOYER role
        if (!"EMPLOYER".equals(auth.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only EMPLOYER accounts can create company profiles");
        }

        // 4. Handle Logo Upload
        String logoUrl = null;
        if (logo != null && !logo.isEmpty()) {
            try {
                logoUrl = azureBlobStorageService.uploadFile(logo);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to upload company logo: " + e.getMessage());
            }
        }

        // 5. Proceed with company creation
        Company company = companyService.createCompany(request, request.getEmployeeId(), logoUrl);
        return ResponseEntity.ok(company);
    }
}
