package com.hotel.scheduler.billing;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class BillingCalculationService {
    private static final int FREE_EMPLOYEES = 5;
    private static final int PRICE_PER_EMPLOYEE = 4;

    /**
     * Calculates the billable amount for an admin based on employee count.
     * @param employeeCount Number of employees for the admin
     * @return The billable amount in USD
     */
    public int calculateBill(int employeeCount) {
        int billableUsers = Math.max(0, employeeCount - FREE_EMPLOYEES);
        return billableUsers * PRICE_PER_EMPLOYEE;
    }

    /**
     * Returns a summary of billing for all admins.
     * @param adminEmployeeCounts Map of admin email to employee count
     * @return Map of admin email to bill amount
     */
    public Map<String, Integer> calculateAllBills(Map<String, Integer> adminEmployeeCounts) {
        Map<String, Integer> bills = new HashMap<>();
        for (Map.Entry<String, Integer> entry : adminEmployeeCounts.entrySet()) {
            bills.put(entry.getKey(), calculateBill(entry.getValue()));
        }
        return bills;
    }
}
