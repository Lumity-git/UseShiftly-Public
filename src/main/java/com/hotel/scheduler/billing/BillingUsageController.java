package com.hotel.scheduler.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/super-admin/admins/usage")
@RequiredArgsConstructor
public class BillingUsageController {
    private final BillingUsageService billingUsageService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAdminUsage() {
        List<Map<String, Object>> usage = billingUsageService.getAllAdminUsage();
        return ResponseEntity.ok(usage);
    }
}
