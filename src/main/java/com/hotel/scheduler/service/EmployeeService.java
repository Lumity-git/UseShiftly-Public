package com.hotel.scheduler.service;

import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.repository.EmployeeRepository;
import com.hotel.scheduler.repository.ShiftTradeRepository;
import com.hotel.scheduler.dto.shift.ShiftTradeResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing Employee entities and related business logic.
 * <p>
 * Handles employee CRUD operations, authentication integration, and shift trade queries.
 * Implements {@link UserDetailsService} for Spring Security authentication.
 * <p>
 * <b>Usage:</b> Injected into controllers and other services for employee-related operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService implements UserDetailsService {

    /**
     * Checks if an employee exists by email.
     *
     * @param email the email to check
     * @return true if an employee with the email exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }

    /**
     * Repository for Employee entity operations.
     */
    private final EmployeeRepository employeeRepository;

    /**
     * Password encoder for hashing employee passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Repository for ShiftTrade entity operations.
     */
    private final ShiftTradeRepository shiftTradeRepository;

    /**
     * Loads an employee by email for authentication.
     *
     * @param email the employee's email (used as username)
     * @return the Employee as UserDetails
     * @throws UsernameNotFoundException if no employee is found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
        return employee;
    }

    /**
     * Creates a new employee, encoding the password and checking for duplicate emails.
     *
     * @param employee the employee to create
     * @return the saved Employee
     * @throws RuntimeException if the email is already taken
     */
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Error: Email is already taken!");
        }
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return employeeRepository.save(employee);
    }

    /**
     * Retrieves all active employees.
     *
     * @return list of active employees
     */
    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findByActiveTrue();
    }

    /**
     * Retrieves all active employees in a specific department.
     *
     * @param departmentId the department ID
     * @return list of employees in the department
     */
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentIdAndActiveTrue(departmentId);
    }

    /**
     * Retrieves an employee by their ID.
     *
     * @param id the employee ID
     * @return optional containing the employee if found
     */
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    /**
     * Retrieves an employee by their email.
     *
     * @param email the employee's email
     * @return optional containing the employee if found
     */
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    /**
     * Updates an existing employee.
     *
     * @param employee the employee to update
     * @return the updated Employee
     */
    public Employee updateEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Deactivates an employee (sets active to false).
     *
     * @param id the employee ID
     * @throws RuntimeException if the employee is not found
     */
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setActive(false);
        employeeRepository.save(employee);
    }

    /**
     * Retrieves all active employees with MANAGER or ADMIN roles.
     *
     * @return list of managers and admins
     */
    public List<Employee> getManagersAndAdmins() {
        return employeeRepository.findByRoleAndActiveTrue(Employee.Role.MANAGER);
    }

    /**
     * Retrieves incoming shift trades for an employee (where pickedUpBy == employeeId).
     *
     * @param employeeId the employee's ID
     * @return list of incoming shift trades as DTOs
     */
    @Transactional(readOnly = true)
    public List<ShiftTradeResponse> getIncomingShiftTrades(Long employeeId) {
        // Find trades where the current employee is the pickup recipient
        var trades = shiftTradeRepository.findByPickupEmployeeId(employeeId);
        // Map entities to DTOs using the static fromEntity method
        return trades.stream()
            .map(ShiftTradeResponse::fromEntity)
            .toList();
    }
}
