package com.useshiftly.scheduler.billing;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class BillingReceiptService {
    private final List<Map<String, Object>> receipts = new ArrayList<>();
    private final BillingEventLogger eventLogger;
    private final EmailService emailService;

    public BillingReceiptService(BillingEventLogger eventLogger, EmailService emailService) {
        this.eventLogger = eventLogger;
        this.emailService = emailService;
    }

    /**
     * Generate and store a receipt for an admin.
     * @param adminEmail Admin's email
     * @param amount Amount billed
     * @param period Billing period (e.g., 2025-07)
     * @return The generated receipt
     */
    public Map<String, Object> generateReceipt(String adminEmail, int amount, String period) {
        Map<String, Object> receipt = new HashMap<>();
        receipt.put("adminEmail", adminEmail);
        receipt.put("amount", amount);
        receipt.put("period", period);
        receipt.put("date", LocalDate.now());
        receipts.add(receipt);
        // Log the receipt generation event for compliance
        if (eventLogger != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("amount", amount);
            details.put("period", period);
            eventLogger.logEvent("RECEIPT_GENERATED", adminEmail, details);
        }
        // Send receipt email
        if (emailService != null) {
            String subject = "Your Monthly Billing Receipt - Period: " + period;
            String body = String.format("Dear Admin,\n\nYour billing receipt for period %s is $%d.\n\nThank you.\n", period, amount);
            emailService.sendReceiptEmail(adminEmail, subject, body);
        }
        return receipt;
    }

    /**
     * Get all receipts for an admin.
     */
    public List<Map<String, Object>> getReceiptsForAdmin(String adminEmail) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> r : receipts) {
            if (r.get("adminEmail").equals(adminEmail)) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Get all receipts (for super admin).
     */
    public List<Map<String, Object>> getAllReceipts() {
        return receipts;
    }
}
