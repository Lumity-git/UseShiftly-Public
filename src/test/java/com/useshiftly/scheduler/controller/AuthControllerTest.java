package com.useshiftly.scheduler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.useshiftly.scheduler.dto.auth.JwtResponse;
import com.useshiftly.scheduler.dto.auth.LoginRequest;
import com.useshiftly.scheduler.model.Building;
import com.useshiftly.scheduler.model.Department;
import com.useshiftly.scheduler.model.Employee;
import com.useshiftly.scheduler.repository.BuildingRepository;
import com.useshiftly.scheduler.repository.DepartmentRepository;
import com.useshiftly.scheduler.repository.EmployeeRepository;
import com.useshiftly.scheduler.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private BuildingRepository buildingRepository;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_ValidCredentials_ReturnsJwtResponse() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@useshiftly.com");
        loginRequest.setPassword("Admin123!");

        Employee mockEmployee = createMockEmployee();
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getPrincipal()).thenReturn(mockEmployee);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("admin@useshiftly.com"));
    }

    @Test
    void login_InvalidCredentials_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@useshiftly.com");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Invalid credentials!"));
    }

    @Test
    void login_UnexpectedError_ReturnsInternalServerError() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@useshiftly.com");
        loginRequest.setPassword("Admin123!");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("Error: An unexpected error occurred during login"));
    }

    private Employee createMockEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmail("admin@useshiftly.com");
        employee.setFirstName("Test");
        employee.setLastName("Admin");
        employee.setRole(Employee.Role.ADMIN);
        employee.setMustChangePassword(false);

        Building building = new Building();
        building.setId(1L);
        building.setName("Test Building");
        employee.setBuilding(building);

        return employee;
    }
}