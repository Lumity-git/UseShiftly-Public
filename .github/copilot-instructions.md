<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# Hotel Employee Scheduler - Copilot Instructions

This is a Spring Boot application for hotel employee scheduling with the following key characteristics:

## Architecture
- **Backend**: Spring Boot 3.2 with Java 17
- **Database**: PostgreSQL (production), H2 (testing)
- **Security**: JWT authentication with Spring Security
- **API**: RESTful endpoints with role-based access control

## Project Structure
```
src/main/java/com/hotel/scheduler/
├── controller/     # REST API endpoints
├── service/        # Business logic layer
├── repository/     # Data access layer (JPA)
├── model/          # Entity classes
├── dto/            # Request/response DTOs
└── security/       # Security configuration and JWT handling
```

## Key Entities
- **Employee**: User accounts with roles (EMPLOYEE, MANAGER, ADMIN)
- **Shift**: Scheduled work periods with date/time and assignments
- **Department**: Hotel departments (Front Desk, Housekeeping, etc.)
- **ShiftTrade**: Tracks shift give-away/pickup requests

## Business Rules
- Employees can only view their own shifts (unless manager/admin)
- Managers can create, update, and delete shifts
- Employees can give away shifts and pick up available shifts
- No scheduling conflicts allowed (same employee, overlapping times)
- Email notifications for shift changes

## Code Style Guidelines
- Use Lombok annotations for reducing boilerplate
- Follow standard Spring Boot patterns
- Use proper validation annotations on DTOs
- Implement proper error handling with meaningful messages
- Use @Transactional for service methods that modify data
- Follow RESTful API conventions

## Security Notes
- All endpoints except `/api/auth/**` require authentication
- Role-based access control using @PreAuthorize
- JWT tokens for stateless authentication
- CORS enabled for frontend integration

## Testing
- Use H2 in-memory database for tests
- Test profiles configured in application-test.yml
- Focus on service layer unit tests and controller integration tests
