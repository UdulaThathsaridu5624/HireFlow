package com.ctse.profileservice.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ctse.profileservice.dto.ValidateResponse;
import com.ctse.profileservice.dto.companyResponseDto;
import com.ctse.profileservice.dto.createCompanyDto;
import com.ctse.profileservice.entity.Company;
import com.ctse.profileservice.service.AuthValidationService;
import com.ctse.profileservice.service.AzureBlobStorageService;
import com.ctse.profileservice.service.companyService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class companyController {

    private final companyService companyService;
    private final AuthValidationService authValidationService;
    private final AzureBlobStorageService azureBlobStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getCompanyByEmployee(@PathVariable String employeeId) {
        return companyService.getCompanyByEmployeeId(employeeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<companyResponseDto>> searchCompanies(@RequestParam(required = false) String query) {
        List<companyResponseDto> results = companyService.searchCompanies(query);
        return ResponseEntity.ok(results);
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<?> updateCompany(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestPart("company") createCompanyDto request,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {

        // Validating auth
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String token = authHeader.substring(7);
        ValidateResponse auth = authValidationService.validate(token);
        if (!auth.isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        // Only allow update if current user is an EMPLOYER
        if (!"EMPLOYER".equals(auth.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only employers can update company profile");
        }

        // Check if the current user owns the company record (security)
        Company existing = companyService.getCompanyById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Optionally verify if employeeId matches current user or allow if they are the
        // owner
        // if (!existing.getEmployeeId().equals(request.getEmployeeId())) ...

        String logoUrl = null;
        if (logo != null && !logo.isEmpty()) {
            try {
                logoUrl = azureBlobStorageService.uploadFile(logo);
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Logo upload failed: " + e.getMessage());
            }
        }

        Company updated = companyService.updateCompany(id, request, logoUrl);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String token = authHeader.substring(7);
        ValidateResponse auth = authValidationService.validate(token);
        if (!auth.isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        if (!"EMPLOYER".equals(auth.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

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
