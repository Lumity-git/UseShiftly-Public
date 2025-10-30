# Public Registration Guide for Shiftly Scheduler

## Overview
Shiftly Scheduler is a free application that allows anyone (individuals or companies) to register and create their own admin account. Each registration creates a new admin user with their own building and departments, ensuring multi-tenant isolation.

## Registration Process

### For New Users
1. **Access Registration Page**: Navigate to `/register-admin.html` in the application.
2. **Fill Registration Form**:
   - Owner Name: Full name of the account owner
   - Building/Business Name: Name of the workplace, business, or organization
   - Building Address: Physical address of the building/business
   - Email: Valid email address (will be used for login)
   - Password: Minimum 8 characters
   - Agree to Terms and Conditions: Required checkbox
3. **Submit Form**: Click "Register" button
4. **Success**: User is redirected to login page with success message

### API Endpoint
- **URL**: `POST /api/auth/register-admin`
- **Content-Type**: `application/json`
- **Request Body**:
```json
{
  "ownerName": "John Doe",
  "buildingName": "Grand Workspace",
  "buildingAddress": "123 Main St, City, State 12345",
  "ownerEmail": "john.doe@example.com",
  "password": "securepassword123"
}
```
- **Response**: Success message with building ID

### What Happens During Registration
1. **Email Validation**: Checks if email is already registered
2. **Admin User Creation**: Creates new Employee with ROLE.ADMIN
3. **Building Creation**: Creates new Building entity owned by the admin
4. **Association**: Links admin to building, building to admin
5. **Password Encryption**: Securely hashes password using bcrypt

## Multi-Tenant Architecture

### Isolation Principles
- Each admin can only access their own building and departments
- Employees invited by an admin are scoped to that admin's building
- All database queries include admin/building ownership checks
- No cross-tenant data access allowed

### Data Scoping
- Buildings: Owned by single admin
- Departments: Belong to specific building
- Employees: Assigned to departments within buildings
- Shifts, Trades, etc.: Scoped to building/department

### Security Measures
- Controller-level ownership validation
- Repository query scoping (e.g., `WHERE building.admin.id = :adminId`)
- IDOR (Insecure Direct Object Reference) prevention
- Audit logging for all actions

## Frontend Implementation

### Files Modified
- `register-admin.html`: Simplified registration form without email verification
- `AdminRegisterRequest.java`: DTO for registration data (removed packageName field)

### Key Features
- Real-time email availability checking
- Form validation (required fields, password length)
- Terms of Service agreement
- Error handling and user feedback
- Automatic redirect to login on success

## Backend Implementation

### Controllers
- `AuthController.registerAdmin()`: Handles registration logic
- `AuthController.checkEmail()`: Validates email uniqueness

### Services
- `EmployeeService.createEmployeeWithUserPassword()`: Creates admin user
- `BuildingRepository.save()`: Persists building

### Models
- `Employee`: User entity with roles and building association
- `Building`: Building entity with admin ownership
- `Department`: Department entity scoped to building

### Security
- Password encryption using BCrypt
- Role-based access control (ADMIN role)
- JWT authentication for session management

## Future Enhancements

### Potential Additions
- Email verification for account security
- Password strength requirements
- Account activation emails
- Profile completion steps
- Organization size selection
- Billing integration (if moving to paid model)

### Scaling Considerations
- Database sharding by tenant
- Caching strategies
- Performance monitoring
- Backup and recovery procedures

## Troubleshooting

### Common Issues
- **Email Already Registered**: Use different email or contact support
- **Form Validation Errors**: Ensure all required fields are filled
- **Server Errors**: Check application logs, database connectivity

### Logs to Check
- Application startup logs
- Registration attempt logs
- Database connection logs

## Maintenance

### Regular Tasks
- Monitor registration success rates
- Clean up unverified accounts (if email verification added)
- Update terms of service as needed
- Backup user data regularly

### Security Audits
- Review access control implementations
- Test multi-tenant isolation
- Validate password policies
- Check for vulnerabilities in dependencies