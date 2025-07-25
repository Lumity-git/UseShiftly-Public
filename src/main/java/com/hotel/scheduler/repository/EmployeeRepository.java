package com.hotel.scheduler.repository;

import com.hotel.scheduler.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Find all employees by admin (scoped to admin's buildings)
    @Query("SELECT e FROM Employee e WHERE e.building.admin.id = :adminId")
    List<Employee> findAllByAdminId(@Param("adminId") Long adminId);
    // Find all employees by department (active or not)
    List<Employee> findByDepartmentId(Long departmentId);
    
    Optional<Employee> findByEmail(String email);
    
    List<Employee> findByActiveTrue();
    
    List<Employee> findByDepartmentIdAndActiveTrue(Long departmentId);
    
    @Query("SELECT e FROM Employee e WHERE e.role = :role AND e.active = true")
    List<Employee> findByRoleAndActiveTrue(@Param("role") Employee.Role role);
    
    boolean existsByEmail(String email);

    // Find all employees by building
    List<Employee> findByBuildingId(Long buildingId);

    /**
     * Returns an employee by ID, eagerly fetching the associated department.
     * @param id Employee ID
     * @return Optional containing the employee if found, empty otherwise
     */
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.id = :id")
    Optional<Employee> findByIdWithDepartment(@Param("id") Long id);

    /**
     * Returns an employee by ID, eagerly fetching the associated building.
     * @param id Employee ID
     * @return Optional containing the employee if found, empty otherwise
     */
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.building WHERE e.id = :id")
    Optional<Employee> findByIdWithBuilding(@Param("id") Long id);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.building.admin.id = :adminId AND e.role = :role")
    long countByAdminIdAndRole(@Param("adminId") Long adminId, @Param("role") com.hotel.scheduler.model.Employee.Role role);
}
