package com.hotel.scheduler.billing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/super-admin/billing")
public class BillingController {
    @Autowired
    private BillingCalculationService billingCalculationService;
    @Autowired
    private BillingReceiptService billingReceiptService;
    @Autowired
    private BillingUsageService billingUsageService;
    @Autowired
    private BillingEventLogger billingEventLogger;
    @Autowired
    private BillingReportService billingReportService;

    // Get all billing events (audit log)
    @GetMapping("/events")
    public List<Map<String, Object>> getAllEvents() {
        return billingEventLogger.getAllEvents();
    }

    // Get billing events for a specific admin
    @GetMapping("/events/{adminEmail}")
    public List<Map<String, Object>> getEventsForAdmin(@PathVariable String adminEmail) {
        return billingEventLogger.getEventsForAdmin(adminEmail);
    }

    // Preview bills for all admins
    @GetMapping("/preview")
    public Map<String, Integer> previewBills() {
        Map<String, Integer> adminEmployeeCounts = billingUsageService.getAdminEmployeeCounts();
        return billingCalculationService.calculateAllBills(adminEmployeeCounts);
    }

    // Generate and store receipts for all admins for a given period
    @PostMapping("/generate-receipts")
    public List<Map<String, Object>> generateReceipts(@RequestParam String period) {
        Map<String, Integer> adminEmployeeCounts = billingUsageService.getAdminEmployeeCounts();
        List<Map<String, Object>> receipts = new java.util.ArrayList<>();
        for (Map.Entry<String, Integer> entry : adminEmployeeCounts.entrySet()) {
            int amount = billingCalculationService.calculateBill(entry.getValue());
            receipts.add(billingReceiptService.generateReceipt(entry.getKey(), amount, period));
        }
        return receipts;
    }

    // Get all receipts (super admin)
    @GetMapping("/receipts")
    public List<Map<String, Object>> getAllReceipts() {
        return billingReceiptService.getAllReceipts();
    }

    // Get receipts for a specific admin
    @GetMapping("/receipts/{adminEmail}")
    public List<Map<String, Object>> getReceiptsForAdmin(@PathVariable String adminEmail) {
        return billingReceiptService.getReceiptsForAdmin(adminEmail);
    }

    // Get total revenue
    @GetMapping("/analytics/total-revenue")
    public int getTotalRevenue() {
        return billingReportService.getTotalRevenue();
    }

    // Get ARPU (average revenue per admin)
    @GetMapping("/analytics/arpu")
    public double getARPU() {
        return billingReportService.getARPU();
    }

    // Get revenue by admin
    @GetMapping("/analytics/revenue-by-admin")
    public Map<String, Integer> getRevenueByAdmin() {
        return billingReportService.getRevenueByAdmin();
    }

    // Export all receipts as CSV
    @GetMapping(value = "/analytics/export-receipts", produces = "text/csv")
    public String exportReceiptsCsv() {
        return billingReportService.exportReceiptsCsv();
    }
}
