package com.hotel.scheduler.repository;

import com.hotel.scheduler.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
    
    List<Department> findByActiveTrue();
    
    Optional<Department> findByNameAndActiveTrue(String name);
    
    boolean existsByName(String name);
}
