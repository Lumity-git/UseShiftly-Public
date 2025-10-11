package com.hotel.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(
    name = "departments",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "building_id"})
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Department {
    // --- Getters ---
    public String getDescription() { return description; }
    public Boolean getActive() { return active; }
    public Integer getMinStaffing() { return minStaffing; }
    public Integer getMaxStaffing() { return maxStaffing; }
    public Integer getTotalShifts() { return totalShifts; }
    public List<Employee> getEmployees() { return employees; }
    public Building getBuilding() { return building; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setActive(Boolean active) { this.active = active; }
    public void setMinStaffing(Integer minStaffing) { this.minStaffing = minStaffing; }
    public void setMaxStaffing(Integer maxStaffing) { this.maxStaffing = maxStaffing; }
    public void setTotalShifts(Integer totalShifts) { this.totalShifts = totalShifts; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }
    public void setBuilding(Building building) { this.building = building; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getId() { return id; }
    public String getName() { return name; }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    @Column(nullable = false)
    public String name;

    @Column
    public String description;

    @Column(nullable = false)
    public Boolean active = true;

    @Column(name = "min_staffing")
    public Integer minStaffing;

    @Column(name = "max_staffing")
    public Integer maxStaffing;

    @Column(name = "total_shifts")
    public Integer totalShifts;


    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // Prevent circular reference during JSON serialization
    public List<Employee> employees;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    public Building building;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department department = (Department) o;
        return id != null && id.equals(department.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}