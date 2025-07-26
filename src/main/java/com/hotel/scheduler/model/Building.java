package com.hotel.scheduler.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
// Removed @Data to prevent Lombok from generating equals/hashCode
public class Building {
    // --- Getters ---
    public Long getId() { return id; }
    public String getName() { return name; }
    public Employee getAdmin() { return admin; }
    public java.util.Set<Employee> getManagers() { return managers; }
    public List<Employee> getEmployees() { return employees; }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAdmin(Employee admin) { this.admin = admin; }
    public void setManagers(java.util.Set<Employee> managers) { this.managers = managers; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Employee admin;

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

    @Column(nullable = false, unique = true)
    public String name;

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