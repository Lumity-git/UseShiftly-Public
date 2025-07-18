package com.hotel.scheduler;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "TempPass123!";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("BCrypt hash for TempPass123!: " + encodedPassword);
    }
}