package com.hotel.scheduler.controller;

import com.hotel.scheduler.dto.auth.JwtResponse;
import com.hotel.scheduler.dto.auth.LoginRequest;
import com.hotel.scheduler.model.Building;
import com.hotel.scheduler.model.Department;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.repository.BuildingRepository;
import com.hotel.scheduler.repository.DepartmentRepository;
import com.hotel.scheduler.repository.EmployeeRepository;
import com.hotel.scheduler.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AuthController focusing on:
 * 1. Password verification with bcrypt hashing
 * 2. Proper JSON response serialization via JwtResponse DTO
 * 3. Exception handling for bad credentials vs unexpected errors
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Building testBuilding;
    private Department testDepartment;
    private static final String TEST_PASSWORD = "Admin123!";
    private static final String TEST_EMAIL = "test.admin@hotel.com";

    @BeforeEach
    public void setup() {
        // Clean up previous test data
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        buildingRepository.deleteAll();

        // Create test building
        testBuilding = new Building();
        testBuilding.setName("Test Building");
        testBuilding.setAddress("123 Test St");
        testBuilding = buildingRepository.save(testBuilding);

        // Create test department
        testDepartment = new Department();
        testDepartment.setName("Test Department");
        testDepartment.setDescription("Test Department Description");
        testDepartment.setBuilding(testBuilding);
        testDepartment.setActive(true);
        testDepartment = departmentRepository.save(testDepartment);
    }

    /**
     * Test that verifies password encoding and matching work correctly.
     * This ensures the stored hash can be validated against the raw password.
     */
    @Test
    public void testPasswordEncodingAndMatching() {
        // Create employee with plain password
        Employee employee = new Employee();
        employee.setEmail(TEST_EMAIL);
        employee.setPassword(TEST_PASSWORD);
        employee.setFirstName("Test");
        employee.setLastName("Admin");
        employee.setRole(Employee.Role.ADMIN);
        employee.setBuilding(testBuilding);
        employee.setDepartment(testDepartment);
        employee.setActive(true);

        // Save employee (should hash the password)
        Employee saved = employeeService.createEmployee(employee, false);

        // Retrieve from database
        Employee retrieved = employeeRepository.findById(saved.getId()).orElseThrow();

        // Verify password is hashed (not plain text)
        assertThat(retrieved.getPassword()).isNotEqualTo(TEST_PASSWORD);
        assertThat(retrieved.getPassword()).startsWith("$2a$"); // BCrypt prefix

        // Verify password matches
        assertThat(passwordEncoder.matches(TEST_PASSWORD, retrieved.getPassword())).isTrue();
        
        // Verify wrong password doesn't match
        assertThat(passwordEncoder.matches("WrongPassword123!", retrieved.getPassword())).isFalse();
    }

    /**
     * Test successful login returns properly serialized JwtResponse.
     * Verifies all expected fields are present in the response.
     */
    @Test
    public void testSuccessfulLoginReturnsJwtResponse() throws Exception {
        // Create test admin user
        Employee admin = new Employee();
        admin.setEmail(TEST_EMAIL);
        admin.setPassword(TEST_PASSWORD);
        admin.setFirstName("Test");
        admin.setLastName("Admin");
        admin.setRole(Employee.Role.ADMIN);
        admin.setBuilding(testBuilding);
        admin.setDepartment(testDepartment);
        admin.setActive(true);
        admin.setMustChangePassword(false);
        employeeService.createEmployee(admin, false);

        // Prepare login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        // Perform login
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.id").value(admin.getId()))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("Admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.mustChangePassword").value(false))
                .andExpect(jsonPath("$.buildingId").value(testBuilding.getId()))
                .andExpect(jsonPath("$.buildingName").value("Test Building"))
                .andReturn();

        // Verify response can be deserialized to JwtResponse
        String responseBody = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseBody, JwtResponse.class);
        
        assertThat(jwtResponse).isNotNull();
        assertThat(jwtResponse.getToken()).isNotBlank();
        assertThat(jwtResponse.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(jwtResponse.getRole()).isEqualTo("ADMIN");
    }

    /**
     * Test that login with bad credentials returns 400 with proper error message.
     * Verifies BadCredentialsException is properly caught and handled.
     */
    @Test
    public void testLoginWithBadCredentialsReturns400() throws Exception {
        // Create test admin user
        Employee admin = new Employee();
        admin.setEmail(TEST_EMAIL);
        admin.setPassword(TEST_PASSWORD);
        admin.setFirstName("Test");
        admin.setLastName("Admin");
        admin.setRole(Employee.Role.ADMIN);
        admin.setBuilding(testBuilding);
        admin.setDepartment(testDepartment);
        admin.setActive(true);
        employeeService.createEmployee(admin, false);

        // Prepare login request with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword("WrongPassword123!");

        // Perform login with wrong credentials
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Invalid credentials!"));
    }

    /**
     * Test that login with non-existent user returns 400.
     */
    @Test
    public void testLoginWithNonExistentUserReturns400() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@hotel.com");
        loginRequest.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Invalid credentials!"));
    }

    /**
     * Test that employee without building can still login successfully.
     * Verifies buildingId and buildingName are null when building is not set.
     */
    @Test
    public void testLoginWithoutBuildingReturnsNullBuildingFields() throws Exception {
        // Create employee without building
        Employee employee = new Employee();
        employee.setEmail("employee@hotel.com");
        employee.setPassword(TEST_PASSWORD);
        employee.setFirstName("Test");
        employee.setLastName("Employee");
        employee.setRole(Employee.Role.EMPLOYEE);
        employee.setDepartment(testDepartment);
        employee.setActive(true);
        employeeService.createEmployee(employee, false);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("employee@hotel.com");
        loginRequest.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("employee@hotel.com"))
                .andExpect(jsonPath("$.buildingId").isEmpty())
                .andExpect(jsonPath("$.buildingName").isEmpty());
    }

    /**
     * Test that mustChangePassword flag is properly returned in login response.
     */
    @Test
    public void testLoginReturnsMustChangePasswordFlag() throws Exception {
        // Create employee with mustChangePassword=true
        Employee employee = new Employee();
        employee.setEmail("newemployee@hotel.com");
        employee.setPassword(TEST_PASSWORD);
        employee.setFirstName("New");
        employee.setLastName("Employee");
        employee.setRole(Employee.Role.EMPLOYEE);
        employee.setBuilding(testBuilding);
        employee.setDepartment(testDepartment);
        employee.setActive(true);
        employee.setMustChangePassword(true);
        employeeService.createEmployee(employee, false);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("newemployee@hotel.com");
        loginRequest.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword").value(true));
    }
}
