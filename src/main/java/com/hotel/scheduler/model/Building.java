package com.hotel.scheduler.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "building")
// Removed @Data to prevent Lombok from generating equals/hashCode
public class Building {
    // --- Getters ---
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public java.util.Set<Employee> getManagers() { return managers; }
    public List<Employee> getEmployees() { return employees; }
    
    /**
     * Gets the admin for this building (first ADMIN role employee assigned to this building)
     */
    public Employee getAdmin() {
        if (employees == null) return null;
        return employees.stream()
            .filter(e -> e.getRole() == Employee.Role.ADMIN)
            .findFirst()
            .orElse(null);
    }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setManagers(java.util.Set<Employee> managers) { this.managers = managers; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }

    @ManyToMany
    @JoinTable(
        name = "building_managers",
        joinColumns = @JoinColumn(name = "building_id"),
        inverseJoinColumns = @JoinColumn(name = "manager_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnore
    public java.util.Set<Employee> managers = new java.util.HashSet<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String address;

    @OneToMany(mappedBy = "building")
    public List<Employee> employees;
    // Only one set of equals/hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Building building = (Building) o;
        return id != null && id.equals(building.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}