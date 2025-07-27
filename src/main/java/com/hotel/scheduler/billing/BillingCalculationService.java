package com.hotel.scheduler.billing;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class BillingCalculationService {
    private static final int FREE_EMPLOYEES = 5;
    private static final int PRICE_BASIC = 4;
    private static final int PRICE_PRO = 7;

    /**
     * Calculates the billable amount for an admin based on employee count and package type.
     * @param employeeCount Number of employees for the admin
     * @param packageType "Basic" or "Pro"
     * @return The billable amount in USD
     */
    public int calculateBill(int employeeCount, String packageType) {
        if ("Pro".equalsIgnoreCase(packageType)) {
            return employeeCount * PRICE_PRO;
        } else {
            int billableUsers = Math.max(0, employeeCount - FREE_EMPLOYEES);
            return billableUsers * PRICE_BASIC;
        }
    }

    /**
     * Returns a summary of billing for all admins.
     * @param adminEmployeeCounts Map of admin email to employee count
     * @param adminPackageTypes Map of admin email to package type
     * @return Map of admin email to bill amount
     */
    public Map<String, Integer> calculateAllBills(Map<String, Integer> adminEmployeeCounts, Map<String, String> adminPackageTypes) {
        Map<String, Integer> bills = new HashMap<>();
        for (Map.Entry<String, Integer> entry : adminEmployeeCounts.entrySet()) {
            String email = entry.getKey();
            int count = entry.getValue();
            String packageType = adminPackageTypes.getOrDefault(email, "Basic");
            bills.put(email, calculateBill(count, packageType));
        }
        return bills;
    }
}
