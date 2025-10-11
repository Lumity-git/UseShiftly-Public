package com.hotel.scheduler.config;

import com.hotel.scheduler.model.Building;
import com.hotel.scheduler.model.Department;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.repository.BuildingRepository;
import com.hotel.scheduler.repository.DepartmentRepository;
import com.hotel.scheduler.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
// Component disabled: data is initialized via schema_rebuild.sql in deployment
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final BuildingRepository buildingRepository;
    private final EmployeeService employeeService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Get or create the main building first. This is the key.
        Building mainBuilding = findOrCreateBuilding("Main Building");
        log.info("Main building created/found with id={}", mainBuilding.getId());

        // Re-fetch a managed reference to guarantee we have an attached entity with id populated
        Building managedBuilding = buildingRepository.findById(mainBuilding.getId()).orElse(mainBuilding);
        log.info("Using managed building with id={}", managedBuilding.getId());

        // 2. Pass the managed building to the other initialization methods.
        initializeDepartments(managedBuilding);
        ensureAdminUser(mainBuilding);
        ensureSuperAdminUser(mainBuilding);
    }

    private Building findOrCreateBuilding(String name) {
        // Find by name, or create a new one if it doesn't exist.
        return buildingRepository.findByName(name)
                .orElseGet(() -> {
                    log.info("Creating new building: {}", name);
                    Building newBuilding = new Building();
                    newBuilding.setName(name);
                    newBuilding.setAddress("123 Main St, Testville, TX"); // Add required fields
                    return buildingRepository.saveAndFlush(newBuilding); // Use saveAndFlush
                });
    }

    private void initializeDepartments(Building building) {
    if (departmentRepository.count() == 0) {
        log.info("Initializing sample departments for building: {}", building.getName());
        log.info("Diagnostic: inserting departments via JDBC for building id={}", building != null ? building.getId() : null);

        // Use JDBC to insert departments directly to avoid JPA mapping issues during bootstrap
        Long buildingId = building.getId();
        jdbcTemplate.update("INSERT INTO departments (name, description, building_id, active, created_at) VALUES (?, ?, ?, ?, NOW())",
            "Front Desk", "Reception and guest services", buildingId, true);
        jdbcTemplate.update("INSERT INTO departments (name, description, building_id, active, created_at) VALUES (?, ?, ?, ?, NOW())",
            "Housekeeping", "Room cleaning and maintenance", buildingId, true);
        jdbcTemplate.update("INSERT INTO departments (name, description, building_id, active, created_at) VALUES (?, ?, ?, ?, NOW())",
            "Restaurant", "Food and beverage service", buildingId, true);
        jdbcTemplate.update("INSERT INTO departments (name, description, building_id, active, created_at) VALUES (?, ?, ?, ?, NOW())",
            "Maintenance", "Building and equipment maintenance", buildingId, true);
    }
    }

    private void createDepartment(String name, String description, Building building) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        // Ensure we use a managed reference/proxy for the association
        Building buildingRef = buildingRepository.getReferenceById(building.getId());
        department.setBuilding(buildingRef); // This is the critical fix.
        log.info("Diagnostic: about to save department '{}' with building id = {} and buildingRef class = {}", name, buildingRef != null ? buildingRef.getId() : null, buildingRef != null ? buildingRef.getClass() : null);
        department.setActive(true);
        try {
            departmentRepository.saveAndFlush(department);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            log.warn("JPA save failed for department '{}', attempting native insert using building_id {}", name, buildingRef.getId());
            // Fallback: insert directly with JDBC using explicit building_id
            jdbcTemplate.update("INSERT INTO departments (name, description, building_id, active, created_at) VALUES (?, ?, ?, ?, NOW())",
                    name, description, buildingRef.getId(), true);
        }
    }

    private void ensureAdminUser(Building mainBuilding) {
        if (!employeeService.existsByEmail("admin@hotel.com")) {
            log.info("Creating default admin user...");
            Department frontDesk = departmentRepository.findByName("Front Desk").orElse(null);

            Employee admin = new Employee();
            admin.setEmail("admin@hotel.com");
            admin.setPassword("admin123");
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRole(Employee.Role.ADMIN);
            admin.setDepartment(frontDesk);
            admin.setBuilding(mainBuilding); // Associate with the building
            admin.setActive(true);
            admin.setMustChangePassword(true);
            admin.setPhoneNumber("555-123-4567");
            admin.setAddress("123 Main St, Testville, TX");

            employeeService.createEmployee(admin, false);
            log.info("Default admin user created: admin@hotel.com");
        }
    }

    private void ensureSuperAdminUser(Building mainBuilding) {
        if (!employeeService.existsByEmail("superadmin@hotel.com")) {
            log.info("Creating default SUPER_ADMIN user...");
            Department frontDesk = departmentRepository.findByName("Front Desk").orElse(null);

            Employee superAdmin = new Employee();
            superAdmin.setEmail("superadmin@hotel.com");
            superAdmin.setPassword("$2a$10$BuuRV7kOJjHapTMJgEI2JuDQlGN1LlGrqzAJLwxDxWYW1weggI/26"); // testpassword123!
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setRole(Employee.Role.SUPER_ADMIN);
            superAdmin.setDepartment(frontDesk);
            superAdmin.setBuilding(mainBuilding); // Associate with the building
            superAdmin.setActive(true);
            superAdmin.setMustChangePassword(false);
            superAdmin.setPhoneNumber("555-000-0000");
            superAdmin.setAddress("1 Admin Plaza, Root City, TX");

            employeeService.createEmployee(superAdmin, false);
            log.info("Default SUPER_ADMIN user created: superadmin@hotel.com");
        }
    }
}