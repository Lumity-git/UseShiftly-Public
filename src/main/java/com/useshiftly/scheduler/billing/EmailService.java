package com.useshiftly.scheduler.billing;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendReceiptEmail(String adminEmail, String subject, String body) {
        // TODO: Integrate with real email provider
        System.out.printf("[EMAIL] To: %s | Subject: %s\n%s\n", adminEmail, subject, body);
    }
}
