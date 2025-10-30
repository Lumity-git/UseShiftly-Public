
package com.useshiftly.scheduler.controller;

import com.useshiftly.scheduler.dto.BulkEmployeeImportResult;
import com.useshiftly.scheduler.model.Employee;
import com.useshiftly.scheduler.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/employees/import")
@RequiredArgsConstructor
@Slf4j
public class BulkEmployeeImportController {
    private final EmployeeService employeeService;

    @PostMapping("/csv")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BulkEmployeeImportResult> importEmployeesCsv(@RequestParam("file") MultipartFile file,
                                                                      @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
                                                                      @RequestHeader(value = "Host", required = false) String host,
                                                                      @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                                                      @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        int total = 0;
        String filename = file.getOriginalFilename();
        String ip = xForwardedFor != null ? xForwardedFor.split(",")[0].trim() : "unknown";
        String user = (principal != null) ? principal.getUsername() : "unknown";
        // 1. File type and extension check
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            log.warn("Bulk import attempt by user={} ip={} host={} userAgent={} - REJECTED: not a CSV file: {}", user, ip, host, userAgent, filename);
            return ResponseEntity.badRequest().body(new BulkEmployeeImportResult(0, 0, 0, List.of("File must be a .csv")));
        }
        if (!"text/csv".equalsIgnoreCase(file.getContentType()) && !"application/vnd.ms-excel".equalsIgnoreCase(file.getContentType())) {
            log.warn("Bulk import attempt by user={} ip={} host={} userAgent={} - REJECTED: invalid file type: {} ({})", user, ip, host, userAgent, filename, file.getContentType());
            return ResponseEntity.badRequest().body(new BulkEmployeeImportResult(0, 0, 0, List.of("Invalid file type. Only CSV allowed.")));
        }
        // 2. File size limit (1MB)
        if (file.getSize() > 1_048_576) {
            log.warn("Bulk import attempt by user={} ip={} host={} userAgent={} - REJECTED: file too large: {} ({} bytes)", user, ip, host, userAgent, filename, file.getSize());
            return ResponseEntity.badRequest().body(new BulkEmployeeImportResult(0, 0, 0, List.of("File too large. Max 1MB.")));
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                log.warn("Bulk import attempt by user={} ip={} host={} userAgent={} - REJECTED: empty file: {}", user, ip, host, userAgent, filename);
                return ResponseEntity.badRequest().body(new BulkEmployeeImportResult(0, 0, 0, List.of("Empty file")));
            }
            String line;
            while ((line = reader.readLine()) != null) {
                total++;
                if (line.matches(".*(<script|</script|<iframe|<img|<svg|<object|<embed|<applet|<form|<input|<body|<html|<link|<style|<meta|<base|<a |javascript:|data:|onerror=|onload=|eval\\().*")) {
                    errors.add("Row " + total + ": Suspicious content detected");
                    continue;
                }
                if (!line.chars().allMatch(c -> c == 9 || (c >= 32 && c <= 126) || c == 10 || c == 13 || c == 44)) {
                    errors.add("Row " + total + ": Non-printable or binary data detected");
                    continue;
                }
                String[] cols = line.split(",");
                if (cols.length < 5) {
                    errors.add("Row " + total + ": Not enough columns");
                    continue;
                }
                try {
                    Employee emp = new Employee();
                    emp.setFirstName(sanitize(cols[0]));
                    emp.setLastName(sanitize(cols[1]));
                    emp.setEmail(sanitize(cols[2]));
                    emp.setPhoneNumber(sanitize(cols[3]));
                    emp.setDateOfBirth(cols.length > 4 ? sanitize(cols[4]) : null);
                    emp.setAddress(cols.length > 5 ? sanitize(cols[5]) : null);
                    emp.setEmergencyContactName(cols.length > 6 ? sanitize(cols[6]) : null);
                    emp.setEmergencyContactRelation(cols.length > 7 ? sanitize(cols[7]) : null);
                    emp.setEmergencyContactPhone(cols.length > 8 ? sanitize(cols[8]) : null);
                    emp.setRole(cols.length > 9 ? Employee.Role.valueOf(sanitize(cols[9]).toUpperCase()) : Employee.Role.EMPLOYEE);
                    emp.setActive(true);
                    emp.setMustChangePassword(true); // Always force password change for imported accounts
                    // Generate a secure random password for imported employees
                    String generatedPassword = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "!";
                    emp.setPassword(generatedPassword);

                    // Store departmentId in a local variable
                    Long departmentId = null;
                    if (cols.length > 10 && cols[10] != null && !cols[10].isBlank()) {
                        try {
                            departmentId = Long.parseLong(sanitize(cols[10]));
                        } catch (NumberFormatException nfe) {
                            errors.add("Row " + total + ": Invalid departmentId format");
                        }
                    }

                    // Create employee and assign department if needed
                    Employee saved = employeeService.createEmployee(emp, true);
                    if (departmentId != null) {
                        employeeService.assignEmployeeToDepartment(saved, departmentId);
                    }
                    success++;
                } catch (Exception ex) {
                    errors.add("Row " + total + ": " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Bulk import failed for user={} ip={} host={} userAgent={} file={}: {}", user, ip, host, userAgent, filename, e.getMessage());
            return ResponseEntity.badRequest().body(new BulkEmployeeImportResult(0, 0, 0, List.of(e.getMessage())));
        }
        log.info("Bulk import by user={} ip={} host={} userAgent={} file={} - totalRows={} success={} errors={}", user, ip, host, userAgent, filename, total, success, errors.size());
        if (!errors.isEmpty()) {
            log.warn("Bulk import errors for user={} file={}: {}", user, filename, errors);
        }
        return ResponseEntity.ok(new BulkEmployeeImportResult(total, success, total - success, errors));
    }

    private String sanitize(String input) {
        if (input == null) return null;
        // Remove < > " ' ` ;
        String clean = input.trim().replaceAll("[<>\\\"'`;]", "");
        return clean.length() > 128 ? clean.substring(0, 128) : clean;
    }
}
