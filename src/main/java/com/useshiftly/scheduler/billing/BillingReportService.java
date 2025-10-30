package com.useshiftly.scheduler.billing;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillingReportService {
    private final BillingReceiptService receiptService;

    public BillingReportService(BillingReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    /**
     * Returns total revenue for all time.
     */
    public int getTotalRevenue() {
        return receiptService.getAllReceipts().stream()
                .mapToInt(r -> (int) r.getOrDefault("amount", 0))
                .sum();
    }

    /**
     * Returns ARPU (average revenue per admin) for all time.
     */
    public double getARPU() {
        List<Map<String, Object>> receipts = receiptService.getAllReceipts();
        Set<String> admins = receipts.stream().map(r -> (String) r.get("adminEmail")).collect(Collectors.toSet());
        if (admins.isEmpty()) return 0.0;
        return (double) getTotalRevenue() / admins.size();
    }

    /**
     * Returns a map of adminEmail to total paid.
     */
    public Map<String, Integer> getRevenueByAdmin() {
        Map<String, Integer> result = new HashMap<>();
        for (Map<String, Object> r : receiptService.getAllReceipts()) {
            String email = (String) r.get("adminEmail");
            int amt = (int) r.getOrDefault("amount", 0);
            result.put(email, result.getOrDefault(email, 0) + amt);
        }
        return result;
    }

    /**
     * Returns all receipts as CSV for export.
     */
    public String exportReceiptsCsv() {
        List<Map<String, Object>> receipts = receiptService.getAllReceipts();
        StringBuilder sb = new StringBuilder();
        sb.append("adminEmail,amount,period,date\n");
        for (Map<String, Object> r : receipts) {
            sb.append(r.get("adminEmail")).append(",")
              .append(r.get("amount")).append(",")
              .append(r.get("period")).append(",")
              .append(r.get("date")).append("\n");
        }
        return sb.toString();
    }
}
