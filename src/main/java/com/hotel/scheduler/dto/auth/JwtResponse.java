package com.hotel.scheduler.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JwtResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean mustChangePassword;
    private Long buildingId;
    private String buildingName;
    
    // Legacy constructor for backward compatibility
    public JwtResponse(String token, String email, String firstName, String lastName, String role) {
        this.token = token;
        this.type = "Bearer";
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.mustChangePassword = false;
    }
}
