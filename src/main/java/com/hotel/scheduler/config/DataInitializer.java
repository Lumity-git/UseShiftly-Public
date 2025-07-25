package com.hotel.scheduler.config;

import com.hotel.scheduler.model.Department;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.repository.DepartmentRepository;
import com.hotel.scheduler.repository.BuildingRepository;
import com.hotel.scheduler.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// @RequiredArgsConstructor removed, using explicit constructor below
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final BuildingRepository buildingRepository;
    private final EmployeeService employeeService;

    public DataInitializer(DepartmentRepository departmentRepository, BuildingRepository buildingRepository, EmployeeService employeeService) {
        this.departmentRepository = departmentRepository;
        this.buildingRepository = buildingRepository;
        this.employeeService = employeeService;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeDepartments();
        ensureAdminUser();
        ensureSuperAdminUser();
    }

    private void initializeDepartments() {
        if (departmentRepository.count() == 0) {
            log.info("Initializing sample departments...");
            Department frontDesk = new Department();
            frontDesk.setName("Front Desk");
            frontDesk.setDescription("Reception and guest services");
            departmentRepository.save(frontDesk);

            Department housekeeping = new Department();
            housekeeping.setName("Housekeeping");
            housekeeping.setDescription("Room cleaning and maintenance");
            departmentRepository.save(housekeeping);

            Department restaurant = new Department();
            restaurant.setName("Restaurant");
            restaurant.setDescription("Food and beverage service");
            departmentRepository.save(restaurant);

            Department maintenance = new Department();
            maintenance.setName("Maintenance");
            maintenance.setDescription("Building and equipment maintenance");
            departmentRepository.save(maintenance);
        }
    }

    private void ensureAdminUser() {
        if (!employeeService.existsByEmail("admin@hotel.com")) {
            // Get department and building from backend
            Department frontDesk = departmentRepository.findByName("Front Desk").orElse(null);
            com.hotel.scheduler.model.Building mainBuilding = null;
            // Use correct return type for findByName
            // If findByName returns Optional<Building>:
            // mainBuilding = buildingRepository.findByName("Main Building").orElse(null);
            // If findByName returns Building directly:
            mainBuilding = buildingRepository.findByName("Main Building");
            // If still null, create and save
            if (mainBuilding == null) {
                mainBuilding = new com.hotel.scheduler.model.Building();
                mainBuilding.setName("Main Building");
                // Set any required fields for Building here
                mainBuilding = buildingRepository.save(mainBuilding);
                log.info("Main Building created and saved to DB");
            }
            if (mainBuilding == null) {
                mainBuilding = new com.hotel.scheduler.model.Building();
                mainBuilding.setName("Main Building");
                // Set any required fields for Building here
                mainBuilding = buildingRepository.save(mainBuilding);
                log.info("Main Building created and saved to DB");
            }

            Employee admin = new Employee();
            admin.setEmail("admin@hotel.com");
            admin.setPassword("admin123");
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRole(Employee.Role.ADMIN);
            admin.setDepartment(frontDesk);
            admin.setBuilding(mainBuilding);
            admin.setActive(true);
            admin.setMustChangePassword(true);
            // Add all possible fields for testing
            admin.setPhoneNumber("555-123-4567");
            admin.setDateOfBirth("1980-01-01");
            admin.setAddress("123 Main St, Testville, TX");
            admin.setEmergencyContactName("Jane Admin");
            admin.setEmergencyContactRelation("Spouse");
            admin.setEmergencyContactPhone("555-987-6543");
            // Any other custom fields can be set here
            employeeService.createEmployee(admin, false);
            log.info("Default admin user created: admin@hotel.com / admin123 with all fields");
        }
    }

    private void ensureSuperAdminUser() {
        if (!employeeService.existsByEmail("superadmin@hotel.com")) {
            Department frontDesk = departmentRepository.findByName("Front Desk").orElse(null);
            com.hotel.scheduler.model.Building mainBuilding = null;
            mainBuilding = buildingRepository.findByName("Main Building");
            if (mainBuilding == null) {
                mainBuilding = new com.hotel.scheduler.model.Building();
                mainBuilding.setName("Main Building");
                mainBuilding = buildingRepository.save(mainBuilding);
                log.info("Main Building created and saved to DB");
            }
            Employee superAdmin = new Employee();
            superAdmin.setEmail("superadmin@hotel.com");
            superAdmin.setPassword("$2a$10$BuuRV7kOJjHapTMJgEI2JuDQlGN1LlGrqzAJLwxDxWYW1weggI/26"); // bcrypt for testpassword123!
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setRole(Employee.Role.SUPER_ADMIN);
            superAdmin.setDepartment(frontDesk);
            superAdmin.setBuilding(mainBuilding);
            superAdmin.setActive(true);
            superAdmin.setMustChangePassword(false);
            superAdmin.setPhoneNumber("555-000-0000");
            superAdmin.setDateOfBirth("1970-01-01");
            superAdmin.setAddress("1 Admin Plaza, Root City, TX");
            superAdmin.setEmergencyContactName("Root Contact");
            superAdmin.setEmergencyContactRelation("None");
            superAdmin.setEmergencyContactPhone("555-111-2222");
            employeeService.createEmployee(superAdmin, false);
            log.info("Default SUPER_ADMIN user created: superadmin@hotel.com / testpassword123!");
        }
    }
}