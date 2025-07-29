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
 * Handles employee CRUD operations, authentication integration, and shift trade
 * queries.
 * Implements {@link UserDetailsService} for Spring Security authentication.
 * <p>
 * <b>Usage:</b> Injected into controllers and other services for
 * employee-related operations.
 */

import org.springframework.context.annotation.Primary;

@Primary
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService implements UserDetailsService {
    /**
     * Creates a new employee (admin or employee) using the password provided by the user,
     * and does NOT set mustChangePassword to true. Used for self-registration flows.
     *
     * @param employee the employee to create
     * @return the saved Employee
     * @throws RuntimeException if the email is already taken
     */
    public Employee createEmployeeWithUserPassword(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Error: Email is already taken!");
        }
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employee.setMustChangePassword(false);
        return employeeRepository.save(employee);
    }
    /**
     * Sends a Stripe invoice email to the given admin.
     * This should generate a Stripe payment link and email it to the admin.
     */
    public void sendStripeInvoice(Employee admin) {
        // TODO: Integrate with StripeService to generate invoice/payment link
        // Example:
        // String stripeLink = stripeService.createInvoiceLink(admin);
        String stripeLink = "https://pay.stripe.com/invoice/demo/" + admin.getId(); // Placeholder
        String subject = "Your Hotel Scheduler Invoice";
        String body = "Hello " + admin.getFirstName() + ",<br><br>"
            + "Please pay your invoice using the following Stripe link:<br>"
            + "<a href='" + stripeLink + "'>Pay Invoice</a><br><br>Thank you.";
        notificationService.sendEmail(admin.getEmail(), subject, body);
    }
    /**
     * Returns the first building assigned to the given manager.
     * @param managerId the manager's employee ID
     * @return Optional<Building> (first found)
     */
    public Optional<com.hotel.scheduler.model.Building> getBuildingForManager(Long managerId) {
        List<com.hotel.scheduler.model.Building> buildings = buildingRepository.findByManagers_Id(managerId);
        if (buildings == null || buildings.isEmpty()) return Optional.empty();
        return Optional.of(buildings.get(0));
    }
    /**
     * Returns all employees for all buildings owned by this admin.
     * Only employees for the admin's buildings are returned.
     */
    public List<Employee> getAllByAdminId(Long adminId) {
        // Defensive: fallback to repository query in case JPA relationships are not always populated
        List<Employee> all = employeeRepository.findAll();
        java.util.List<Employee> result = new java.util.ArrayList<>();
        for (Employee e : all) {
            if (e.getBuilding() != null && e.getBuilding().getAdmin() != null && e.getBuilding().getAdmin().getId().equals(adminId)) {
                result.add(e);
            }
        }
        return result;
    }

    public Optional<com.hotel.scheduler.model.Building> getBuildingById(Long buildingId) {
        return buildingRepository.findById(buildingId);
    }

    public Optional<com.hotel.scheduler.model.Department> getDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId);
    }

    
    private final com.hotel.scheduler.repository.BuildingRepository buildingRepository;

    /**
     * Assigns a single employee to a building.
     * 
     * @param employee   the employee entity
     * @param buildingId the building ID
     */
    public void assignEmployeeToBuilding(Employee employee, Long buildingId) {
        var building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Building not found"));
        employee.setBuilding(building);
        employeeRepository.save(employee);
    }

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
     * Returns all employees assigned to a specific building.
     * 
     * @param buildingId the building ID
     * @return list of employees in the building
     */
    public List<Employee> getEmployeesByBuilding(Long buildingId) {
        return employeeRepository.findByBuildingId(buildingId);
    }

    /**
     * Password encoder for hashing employee passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Repository for ShiftTrade entity operations.
     */
    private final ShiftTradeRepository shiftTradeRepository;
    /**
     * Repository for Department entity operations.
     */
    private final com.hotel.scheduler.repository.DepartmentRepository departmentRepository;

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
     * Assigns a single employee to a department.
     * 
     * @param employee     the employee entity
     * @param departmentId the department ID
     */
    public void assignEmployeeToDepartment(Employee employee, Long departmentId) {
        var department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        employee.setDepartment(department);
        employeeRepository.save(employee);
    }

    /**
     * Assigns multiple employees to a department.
     * 
     * @param employeeIds  list of employee IDs
     * @param departmentId the department ID
     */
    public void assignEmployeesToDepartment(List<Long> employeeIds, Long departmentId) {
        var department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        var employees = employeeRepository.findAllById(employeeIds);
        for (Employee employee : employees) {
            employee.setDepartment(department);
        }
        employeeRepository.saveAll(employees);
    }

    /**
     * Creates a new employee, encoding the password and checking for duplicate
     * emails.
     *
     * @param employee the employee to create
     * @return the saved Employee
     * @throws RuntimeException if the email is already taken
     */
    private final NotificationService notificationService;

    /**
     * Creates a new employee, generates a temp password, sets mustChangePassword,
     * and sends registration email if created by admin/manager.
     * 
     * @param employee                the employee to create
     * @param createdByAdminOrManager true if created by admin/manager, false if
     *                                self-registration
     * @return the saved Employee
     */
    public Employee createEmployee(Employee employee, boolean createdByAdminOrManager) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Error: Email is already taken!");
        }
        String tempPassword = employee.getPassword();
        if (createdByAdminOrManager) {
            // Generate random password
            tempPassword = java.util.UUID.randomUUID().toString().substring(0, 10);
            employee.setPassword(tempPassword);
            employee.setMustChangePassword(true);
        }
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        Employee saved = employeeRepository.save(employee);
        if (createdByAdminOrManager) {
            notificationService.sendEmployeeRegistrationEmail(saved, tempPassword);
        }
        return saved;
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
     * Soft deletes an employee by setting deleted_at timestamp.
     * @param id Employee ID
     */
    public void deleteEmployee(Long id) {
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            // Add the deletedAt field if not present in Employee class:
            // private LocalDateTime deletedAt;
            // And its setter:
            // public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
            employee.setDeletedAt(java.time.LocalDateTime.now());
            employeeRepository.save(employee);
        }
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
     * Checks if the provided password matches the employee's current password.
     */
    public boolean checkPassword(Employee employee, String rawPassword) {
        return passwordEncoder.matches(rawPassword, employee.getPassword());
    }

    /**
     * Updates the employee's password and sets mustChangePassword to false.
     */
    public void updatePassword(Employee employee, String newPassword) {
        employee.setPassword(passwordEncoder.encode(newPassword));
        employee.setMustChangePassword(false);
        employeeRepository.save(employee);
    }

    /**
     * Retrieves incoming shift trades for an employee (where pickedUpBy ==
     * employeeId).
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

    public Optional<Employee> getEmployeeWithBuilding(Long id) {
        return employeeRepository.findByIdWithBuilding(id);
    }
    /**
     * Checks if the given employee is the admin of the specified building.
     */
    public boolean isAdminOfBuilding(Employee employee, Long buildingId) {
        return buildingRepository.findById(buildingId)
                .map(b -> b.getAdmin() != null && b.getAdmin().getId().equals(employee.getId()))
                .orElse(false);
    }

    /**
     * Checks if the given employee is the manager of the specified building.
     */
    public boolean isManagerOfBuilding(Employee employee, Long buildingId) {
        return buildingRepository.findById(buildingId)
                .map(b -> b.getManagers() != null && b.getManagers().stream().anyMatch(m -> m.getId().equals(employee.getId())))
                .orElse(false);
    }

    /**
     * Checks if the given employee is the admin of the specified department (via building).
     */
    public boolean isAdminOfDepartment(Employee employee, Long departmentId) {
        return departmentRepository.findById(departmentId)
                .map(d -> d.getBuilding() != null && d.getBuilding().getAdmin() != null && d.getBuilding().getAdmin().getId().equals(employee.getId()))
                .orElse(false);
    }

    /**
     * Checks if the given employee is the manager of the specified department (via building).
     */
    public boolean isManagerOfDepartment(Employee employee, Long departmentId) {
        return departmentRepository.findById(departmentId)
                .map(d -> d.getBuilding() != null && d.getBuilding().getManagers() != null && d.getBuilding().getManagers().stream().anyMatch(m -> m.getId().equals(employee.getId())))
                .orElse(false);
    }

    // --- Super-admin methods for admin management ---
    /**
     * Returns all admin accounts (for super-admin only).
     */
    public List<Employee> getAllAdmins() {
        return employeeRepository.findByRoleAndActiveTrue(Employee.Role.ADMIN);
    }

    /**
     * Creates a new admin account (for super-admin only).
     */
    public Employee createAdmin(Employee admin, String password) {
        if (employeeRepository.existsByEmail(admin.getEmail())) {
            throw new RuntimeException("Error: Email is already taken!");
        }
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Employee.Role.ADMIN);
        admin.setActive(true);
        // Ensure packageType is set, default to Basic if missing
        if (admin.getPackageType() == null || admin.getPackageType().isEmpty()) {
            admin.setPackageType("Basic");
        }
        return employeeRepository.save(admin);
    }

    /**
     * Gets an admin by ID (for super-admin only).
     */
    public Optional<Employee> getAdminById(Long id) {
        return employeeRepository.findById(id)
                .filter(e -> e.getRole() == Employee.Role.ADMIN);
    }

    /**
     * Deletes an admin by ID (for super-admin only).
     */
    public void deleteAdmin(Long id) {
        Optional<Employee> adminOpt = getAdminById(id);
        if (adminOpt.isEmpty()) {
            throw new RuntimeException("Admin not found");
        }
        employeeRepository.deleteById(id);
    }
}
