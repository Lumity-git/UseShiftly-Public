
package com.useshiftly.scheduler.dto.auth;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class AdminRegisterRequest {
    @NotBlank(message = "Building name is required")
    private String buildingName;

    @NotBlank(message = "Building address is required")
    private String buildingAddress;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Business email is required")
    @Email(message = "Please provide a valid email")
    private String ownerEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
