package com.hotel.scheduler.config;

import com.hotel.scheduler.model.Department;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.repository.DepartmentRepository;
import com.hotel.scheduler.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// @RequiredArgsConstructor removed, using explicit constructor below
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final EmployeeService employeeService;

    public DataInitializer(DepartmentRepository departmentRepository, EmployeeService employeeService) {
        this.departmentRepository = departmentRepository;
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
            Department frontDesk = departmentRepository.findByName("Front Desk").orElse(null);
            Employee admin = new Employee();
            admin.setEmail("admin@hotel.com");
            admin.setPassword("admin123");
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRole(Employee.Role.ADMIN);
            admin.setDepartment(frontDesk);
            employeeService.createEmployee(admin, false);
            log.info("Default admin user created: admin@hotel.com / admin123");
        }
    }
}