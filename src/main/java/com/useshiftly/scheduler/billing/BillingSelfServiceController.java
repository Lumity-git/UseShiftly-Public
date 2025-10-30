package com.useshiftly.scheduler.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/billing")
public class BillingSelfServiceController {
    @Autowired
    private BillingReceiptService billingReceiptService;

    // Get receipts for the logged-in admin (stub: adminEmail as param)
    @GetMapping("/receipts/{adminEmail}")
    public List<Map<String, Object>> getReceipts(@PathVariable String adminEmail) {
        return billingReceiptService.getReceiptsForAdmin(adminEmail);
    }

    // Show current usage and projected next bill (stub)
    @GetMapping("/projected-bill/{adminEmail}")
    public Map<String, Object> getProjectedBill(@PathVariable String adminEmail) {
        // TODO: Integrate with real usage and billing logic
        return Map.of(
            "adminEmail", adminEmail,
            "projectedAmount", 0
        );
    }
}
