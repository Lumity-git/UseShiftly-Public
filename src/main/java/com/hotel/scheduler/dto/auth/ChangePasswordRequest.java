package com.hotel.scheduler.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String newPassword;

    // For password reset via link
    private String code;
    private String token;
}
