package com.useshiftly.scheduler;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGen {
    public static void main(String[] args) {
        String password = "Admin123!";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        System.out.println("BCrypt hash for '" + password + "': " + hash);
        
        // Verify the hash works
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);
    }
}
