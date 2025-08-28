package com.hotel.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "employees")
public class Employee implements UserDetails {
    /**
     * Billing package type for admin: "Basic" or "Pro".
     * Basic: 5 free users, $4/user/month after 5, website/email access.
     * Pro: $7/user/month, no free users, website/email/mobile app access.
     */
    @Column(name = "package_type", nullable = false)
    private String packageType = "Basic"; // Set to "Pro" for Pro tier

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }
    // --- Setters ---
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    public void setId(Long id) { this.id = id; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRole(Role role) { this.role = role; }
    public void setDepartment(Department department) { this.department = department; }
    public void setBuilding(Building building) { this.building = building; }
    public void setActive(Boolean active) { this.active = active; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setAddress(String address) { this.address = address; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    public void setEmergencyContactRelation(String emergencyContactRelation) { this.emergencyContactRelation = emergencyContactRelation; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public Role getRole() { return role; }
    public Department getDepartment() { return department; }
    public Building getBuilding() { return building; }
    public Boolean getActive() { return active; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public String getEmergencyContactRelation() { return emergencyContactRelation; }
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    @Column(name = "must_change_password", nullable = false)
    public boolean mustChangePassword = false;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, updatable = false)
    public String uuid = java.util.UUID.randomUUID().toString();
    
    @Column(unique = true, nullable = false)
    public String email;
    
    @Column(nullable = false)
    public String password;
    
    @Column(nullable = false)
    public String firstName;
    
    @Column(nullable = false)
    public String lastName;
    
    @Column
    public String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Role role = Role.EMPLOYEE;
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnore // Prevent circular reference during JSON serialization
    public Department department;


    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnore
    public Building building;
    
    @Column(nullable = false)
    public Boolean active = true;

    @Column(name = "date_of_birth")
    public String dateOfBirth; // ISO format (yyyy-MM-dd)

    @Column(name = "address")
    public String address;

    @Column(name = "emergency_contact_name")
    public String emergencyContactName;

    @Column(name = "emergency_contact_relation")
    public String emergencyContactRelation;

    @Column(name = "emergency_contact_phone")
    public String emergencyContactPhone;
    
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
    
    public enum Role {
        EMPLOYEE, MANAGER, ADMIN, SUPER_ADMIN
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return id != null && id.equals(employee.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
