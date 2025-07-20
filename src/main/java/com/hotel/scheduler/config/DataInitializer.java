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
}