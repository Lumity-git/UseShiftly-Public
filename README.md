# UseShiftly Shift Scheduler

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![Status](https://img.shields.io/badge/Status-Production%20Ready-blue)
![Security](https://img.shields.io/badge/Security-Audited-success)

A professional, enterprise-ready scheduling application built with Spring Boot for cross-industry shift team management. This system replaces traditional whiteboard scheduling with a modern, digital solution that includes shift trading, notifications, and role-based access control.

## 🚀 Features

### Core Functionality
- **Employee Management**: Role-based access (Manager, Employee, Admin)
- **Shift Scheduling**: Create, view, and manage shifts with conflict detection
- **Shift Trading**: Employees can give away and pick up shifts seamlessly
- **Real-time Notifications**: Email notifications for all shift changes
- **Department Management**: Organize employees by business departments
- **Mobile-Ready API**: RESTful API ready for mobile/web frontend integration

### Security & Access Control
- **JWT Authentication**: Secure, stateless authentication
- **Role-Based Permissions**: Different access levels for different roles
- **Data Protection**: Employees only see their own shifts (unless manager/admin)

### Business Features
- **Conflict Prevention**: Automatic detection of scheduling conflicts
- **Audit Trail**: Track who created/modified shifts
- **Department Filtering**: View shifts by department or employee
- **Date Range Queries**: Filter shifts by custom date ranges

## 📸 Preview

![Scheduler Dashboard](https://via.placeholder.com/1200x675.png?text=Scheduler+Dashboard+Preview)

## 🛠 Tech Stack

- **Backend**: Spring Boot 3.2, Java 21
- **Database**: PostgreSQL (production), H2 (testing)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Spring Boot Test
- **Security**: JWT, Spring Security 6
- **Database Migration**: Flyway
- **Deployment**: Docker & Docker Compose ready

## 🔧 Environment Variables

The application uses the following environment variables for configuration:

### Required Variables
- `DB_USERNAME` - PostgreSQL database username
- `DB_PASSWORD` - PostgreSQL database password
- `JWT_SECRET` - Secret key for JWT token signing

### Optional Variables
- `ALLOWED_ORIGINS` - Comma-separated list of allowed CORS origins (default: `https://example.com`)
- `NOTIFICATION_BASE_URL` - Base URL for notification links (default: `https://example.com`)
- `EMAIL_FROM` - Sender email address (default: `noreply@example.com`)
- `EMAIL_ENABLED` - Enable/disable email notifications (default: `true`)
- `MAIL_HOST` - SMTP server host (default: `smtp.gmail.com`)
- `MAIL_PORT` - SMTP server port (default: `587`)
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password
- `RATE_LIMIT_RPM` - Rate limit requests per minute (default: `60`)
- `ABUSE_DETECTION_ENABLED` - Enable abuse detection (default: `true`)
- `ENHANCED_LOGGING` - Enable structured security logging (default: `true`)

### Application Profiles
- `dev` - Development profile for local testing (configure sample data manually)
- `prod` - Production profile using Flyway migrations and externalized secrets

   ```bash
   git clone <repository>

   spring:
       url: jdbc:postgresql://localhost:5432/scheduler_db
       username: your_username
       password: your_password
   ```

4. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

### Option 2: Docker Compose (Recommended)
```bash
# Build and start everything
docker-compose up -d

# View logs
docker-compose logs -f app
```

### Default Users
The system seeds placeholder accounts for testing. Update credentials before going live:
- **Admin**: `admin@example.com` / `<choose-strong-password>`
- **Manager**: `manager@example.com` / `<choose-strong-password>`
- **Employee**: `employee@example.com` / `<choose-strong-password>`

## 📚 API Documentation

### Authentication Endpoints
```http
POST /api/auth/login
POST /api/auth/register
```

### Employee Management
```http
GET /api/employees          # List all employees (Manager+)
GET /api/employees/me       # Get current user profile
PUT /api/employees/me       # Update current user profile
GET /api/employees/department/{id}  # Get employees by department
```

### Shift Management
```http
GET /api/shifts             # Get shifts (filtered by role)
GET /api/shifts/my-shifts   # Get current user's shifts
GET /api/shifts/available   # Get shifts available for pickup
POST /api/shifts            # Create new shift (Manager+)
PUT /api/shifts/{id}        # Update shift (Manager+)
DELETE /api/shifts/{id}     # Delete shift (Manager+)
```

### Shift Trading
```http
POST /api/shifts/{id}/give-away    # Make shift available for pickup
POST /api/shifts/{id}/pick-up      # Pick up an available shift
```

### Example API Usage

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"manager@example.com","password":"<choose-strong-password>"}'
```

**Create Shift:**
```bash
curl -X POST http://localhost:8080/api/shifts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "startTime": "2024-12-15 09:00:00",
    "endTime": "2024-12-15 17:00:00",
    "employeeId": 1,
    "departmentId": 1,
    "notes": "Front desk morning shift"
  }'
```

## 🏗 Architecture

```
src/main/java/com/useshiftly/scheduler/
├── controller/     # REST API endpoints
├── service/        # Business logic layer
├── repository/     # Data access layer (JPA)
├── model/          # Entity classes
├── dto/            # Request/response DTOs
├── security/       # Security configuration and JWT handling
└── config/         # Application configuration
```

### Database Schema
- **employees**: User accounts with roles and contact info
- **departments**: Business departments (Front Desk, Housekeeping, etc.)
- **shifts**: Scheduled work periods with date/time and assignments
- **shift_trades**: Tracks shift give-away/pickup requests

## 🔒 Security Features

- **JWT-based authentication** with configurable expiration
- **Role-based access control** (Employee, Manager, Admin)
- **CORS configuration** for frontend integration
- **Password encryption** using BCrypt
- **Input validation** on all API endpoints

## 🚀 Deployment

### Production Configuration
1. **Set environment variables**:
   ```bash
   export DB_USERNAME=your_db_user
   export DB_PASSWORD=your_db_password
   export JWT_SECRET=your_secure_jwt_secret
   export MAIL_HOST=your_smtp_host
   ```

2. **Build for production**:
   ```bash
   mvn clean package -Pprod
   ```

3. **Deploy with Docker**:
   ```bash
   docker build -t scheduler-app .
   docker run -p 8080:8080 scheduler-app
   ```

### Cloud Deployment Options
- **AWS**: Use Elastic Beanstalk or ECS
- **Azure**: App Service or Container Instances
- **Google Cloud**: Cloud Run or App Engine
- **DigitalOcean**: App Platform
- **Railway/Render**: Direct deployment from Git

## 📧 Email Notifications

Configure SMTP settings for automated notifications:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

Notifications are sent for:
- New shift assignments
- Shift updates/changes
- Shift cancellations
- Shift pickup confirmations

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## 📈 Monitoring & Maintenance

The application includes:
- **Health check endpoints** for monitoring
- **Structured logging** for debugging
- **Error handling** with meaningful messages
- **Database migration** support with Flyway (optional)

## 💼 Business Value

This application solves real workforce management problems:
- **Eliminates manual whiteboard scheduling**
- **Reduces scheduling conflicts and confusion**
- **Improves employee communication**
- **Provides audit trail for shift changes**
- **Enables flexible shift trading**
- **Scales with business growth**

## 🔄 Future Enhancements

Potential features for future releases:
- Mobile app (React Native/Flutter)
- Time clock integration
- Payroll system integration
- Advanced reporting and analytics
- Multi-location support
- Automated shift scheduling with AI
- SMS notifications
- Calendar integration (Google Calendar, Outlook)

## 📞 Support

For technical support or feature requests, contact:
- **Developer**: [Your Contact Info]
- **Documentation**: Available in `/docs` folder
- **API Docs**: Available at `/swagger-ui.html` (when enabled)

## 📄 License

MIT License - see [LICENSE](LICENSE)

---

**This is a production-ready application designed specifically for shift scheduling. It replaces manual processes with automated, digital solutions that improve efficiency and employee satisfaction.**
<!-- API-REFERENCE-START -->
## Developer API Reference
_Since v2.0.0_

### Authentication & Invitation Management

#### AuthController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| POST registerAdmin | Create a building admin and associated building. | POST `/api/auth/register-admin` | Body: `AdminRegisterRequest` {ownerEmail (String, required), ownerName (String, required), password (String, required), buildingName (String, required), buildingAddress (String, required)} | 200 JSON `message` with building ID on success. | 400 JSON `message` when email exists or persistence fails. | `curl -X POST /api/auth/register-admin -H 'Content-Type: application/json' -d '{"ownerEmail":"admin@example.com"}'` |
| GET checkEmail | Check if email is registered. | GET `/api/auth/check-email` | Query: `email` (String, required) | 200 JSON `{ "registered": boolean }`. | 400 on invalid inputs. | `curl '/api/auth/check-email?email=foo@bar.com'` |
| POST changePassword | Change current or invited user's password. | POST `/api/auth/change-password` | Body: `ChangePasswordRequest` {newPassword (String, required), code/token (String, optional for invite flow)}; Auth principal optional. | 200 message on success. | 400 invalid password pattern or errors; 401 when unauthenticated or invalid reset link; 404 when invited user missing. | `curl -X POST /api/auth/change-password -H 'Content-Type: application/json' -d '{"newPassword":"Abc123!@#"}'` |
| GET me | Return authenticated user profile summary. | GET `/api/auth/me` | Auth principal required. | 200 JSON with id, email, names, role, department/building info. | 401 when principal null. | `curl -H 'Authorization: Bearer <token>' /api/auth/me` |
| POST login | Authenticate user and issue JWT. | POST `/api/auth/login` | Body: `LoginRequest` {email (String), password (String)} | 200 JSON string containing token and user details. | 400 invalid credentials. | `curl -X POST /api/auth/login -d '{"email":"user","password":"<choose-strong-password>"}'` |
| POST register | Register invited user. | POST `/api/auth/register` | Form-data `RegisterRequest` {invitationCode (String), invitationToken (String), email, password, firstName, lastName, phoneNumber?, dateOfBirth?, address?, emergency contacts?, departmentId?} | 200 message success. | 400 missing/invalid invitation or duplicates; 401 invalid invitation; 403 mismatched department. | `curl -X POST -F 'invitationCode=...' -F 'email=...' /api/auth/register` |
| GET validateInvitation | Validate invitation token. | GET `/api/auth/validate-invitation` | Query: `code` (String), `token` (String). | 200 JSON invitation details. | 400 invalid; 401 expired invitation. | `curl '/api/auth/validate-invitation?code=...&token=...'` |
| POST generateInvitation | Create invitation for employee or reset. | POST `/api/auth/generate-invitation` | Body Map {email (String, required), role (String, optional default EMPLOYEE), departmentId?, departmentName?, type?, buildingId?, buildingName?}; Auth principal with MANAGER/ADMIN. | 200 JSON codes, token, expiry, optional building/department. | 400 missing email/building or validation errors. | `curl -X POST /api/auth/generate-invitation -H 'Authorization: Bearer <token>' -d '{"email":"new@staff.com"}'` |
| GET validateToken | Validate JWT for current user. | GET `/api/auth/validate` | Auth principal optional. | 200 JSON user metadata when authenticated. | 400 invalid token. | `curl -H 'Authorization: Bearer <token>' /api/auth/validate` |
| POST refreshToken | Refresh JWT token. | POST `/api/auth/refresh` | Auth principal required. | 200 JSON string with refreshed token and user info. | 401 when principal missing or failure. | `curl -X POST -H 'Authorization: Bearer <token>' /api/auth/refresh` |
| GET activeInvitations | List active invitations. | GET `/api/auth/active-invitations` | Auth principal (MANAGER/ADMIN). | 200 JSON array of invitations. | 400 errors retrieving data. | `curl -H 'Authorization: Bearer <token>' /api/auth/active-invitations` |
| DELETE deleteInvitation | Delete invitation by code. | DELETE `/api/auth/delete-invitation/{code}` | Path `code` (String), Auth principal (MANAGER/ADMIN). | 200 message on success; 404 message when missing. | 400 on service exceptions. | `curl -X DELETE /api/auth/delete-invitation/INV-1234` |

#### AdminAccessController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| POST requestAdminAccess | Trigger verification email for prospective admin. | POST `/api/auth/request-admin-access` | Body: `AdminAccessRequest` {email (String, required), ... _documentation needed_}; rate-limited by IP. | 200 message when code sent. | 400 missing email or already registered; 429 when rate limited; 500 email failure. | `curl -X POST /api/auth/request-admin-access -d '{"email":"owner@example.com"}'` |
| POST verifyAdminCode | Validate emailed admin verification code. | POST `/api/auth/verify-admin-code` | Body: `AdminCodeVerificationRequest` {email (String, required), code (String, required)}. | 200 plain success text. | 401 invalid/expired code; 429 when rate limited. | `curl -X POST /api/auth/verify-admin-code -d '{"email":"owner@example.com","code":"123456"}'` |

#### SuperAdminAuthController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| POST superAdminLogin | Authenticate super admin user and return JWT. | POST `/api/super-admin/auth/login` | Body Map {email (String, required), password (String, required)}. | 200 JSON with token and profile. | 403 when authenticated non-super-admin; 401 invalid credentials. | `curl -X POST /api/super-admin/auth/login -d '{"email":"root@example.com","password":"<choose-strong-password>"}'` |

### Security Operations

#### SecurityManagementController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| GET getSecurityDashboard | Retrieve aggregated security metrics. | GET `/api/super-admin/security/dashboard` | None (SUPER_ADMIN). | 200 JSON with rate limiting, abuse detection, events, system status. | 500 retrieval failure. | `curl -H 'Authorization: Bearer <token>' /api/super-admin/security/dashboard` |
| GET getRateLimitStatus | Inspect rate limiting state for an IP. | GET `/api/super-admin/security/rate-limit/{ip}` | Path `ip` (String). | 200 JSON with block flags and counters. | 500 on service errors. | `curl /api/super-admin/security/rate-limit/203.0.113.5` |
| POST blockIP | Manually block an IP. | POST `/api/super-admin/security/block-ip` | Body {ip (String, required), reason (String, optional)}. | 200 message + metadata. | 400 missing IP; 500 service failure. | `curl -X POST /api/super-admin/security/block-ip -d '{"ip":"203.0.113.5"}'` |
| POST unblockIP | Remove IP from block list. | POST `/api/super-admin/security/unblock-ip` | Body {ip (String, required)}. | 200 message with IP. | 400 missing IP; 500 errors. | `curl -X POST /api/super-admin/security/unblock-ip -d '{"ip":"203.0.113.5"}'` |
| POST whitelistIP | Add IP to whitelist. | POST `/api/super-admin/security/whitelist-ip` | Body {ip (String, required)}. | 200 message with IP. | 400 missing IP; 500 errors. | `curl -X POST /api/super-admin/security/whitelist-ip -d '{"ip":"203.0.113.5"}'` |
| POST blacklistIP | Permanently blacklist IP. | POST `/api/super-admin/security/blacklist-ip` | Body {ip (String, required)}. | 200 message with IP. | 400 missing IP; 500 errors. | `curl -X POST /api/super-admin/security/blacklist-ip -d '{"ip":"203.0.113.5"}'` |
| POST removeFromBlacklist | Remove IP from permanent blacklist. | POST `/api/super-admin/security/remove-from-blacklist` | Body {ip (String, required)}. | 200 message with IP. | 400 missing IP; 500 errors. | `curl -X POST /api/super-admin/security/remove-from-blacklist -d '{"ip":"203.0.113.5"}'` |
| GET getSecurityConfig | Fetch active security configuration snapshot. | GET `/api/super-admin/security/config` | None. | 200 JSON describing rate limiting, abuse detection, monitoring. | 500 retrieval failure. | `curl /api/super-admin/security/config` |
| GET getSecurityHealth | Retrieve security subsystem health. | GET `/api/super-admin/security/health` | None. | 200 JSON status summary. | 500 retrieval failure. | `curl /api/super-admin/security/health` |
| POST triggerCleanup | Manually trigger security cleanup. | POST `/api/super-admin/security/cleanup` | None. | 200 message with timestamp. | 500 cleanup failure. | `curl -X POST /api/super-admin/security/cleanup` |

### Employee & Organization Management

#### EmployeeController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| GET getEmployeesForMyBuilding | List non-admin employees for manager's building. | GET `/api/employees/my-building` | Auth principal (MANAGER). | 200 array of `EmployeeDTO`. | 404 when manager has no building. | `curl -H 'Authorization: Bearer <manager>' /api/employees/my-building` |
| DELETE deleteEmployee | Permanently delete employee. | DELETE `/api/employees/{id}/delete` | Path `id` (Long), Auth ADMIN. | 200 message success. | 400 service errors. | `curl -X DELETE /api/employees/42/delete` |
| GET getEmployeesByBuilding | List employees for building. | GET `/api/employees/by-building/{buildingId}` | Path `buildingId` (Long), Auth MANAGER/ADMIN. | 200 array of `EmployeeDTO`. | 403 when not authorized. | `curl /api/employees/by-building/7` |
| PUT updateEmployee | Update employee attributes. | PUT `/api/employees/{id}` | Path `id` (Long), Body Map fields (names, contact, role, departmentId, buildingId, active), Auth MANAGER/ADMIN. | 200 updated `EmployeeDTO`. | 400 invalid values; 403 unauthorized. | `curl -X PUT /api/employees/42 -d '{"firstName":"Alex"}'` |
| GET exportEmployeesCsv | Export CSV of active employees. | GET `/api/employees/export` | Auth MANAGER/ADMIN. | 200 `text/csv` string. | None specified. | `curl -H 'Accept:text/csv' /api/employees/export` |
| GET getEligibleEmployeesForTrade | List other employees for shift trade. | GET `/api/employees/eligible-for-trade` | Auth ANY role. | 200 array of `EmployeeDTO`. | None. | `curl /api/employees/eligible-for-trade` |
| GET getEmployeeById | Fetch employee by ID. | GET `/api/employees/{id}` | Path `id` (Long), Auth MANAGER/ADMIN. | 200 `EmployeeDTO`. | 404 not found; 400 errors. | `curl /api/employees/42` |
| GET getAllEmployees | List employees for admin/manager. | GET `/api/employees` | Auth MANAGER/ADMIN. | 200 array `EmployeeDTO`. | None. | `curl /api/employees` |
| GET getCurrentEmployee | Self profile. | GET `/api/employees/me` | Auth principal. | 200 `EmployeeDTO`. | None. | `curl /api/employees/me` |
| GET getEmployeeProfile | Profile with building info. | GET `/api/employees/profile` | Auth principal. | 200 `EmployeeDTO`. | 404 when missing. | `curl /api/employees/profile` |
| PUT updateCurrentEmployee | Self-update contact info. | PUT `/api/employees/me` | Body Map {firstName?, lastName?, phoneNumber?}, Auth principal. | 200 updated `EmployeeDTO`. | 400 errors. | `curl -X PUT /api/employees/me -d '{"phoneNumber":"555-0101"}'` |
| GET getEmployeesByDepartment | List employees by department. | GET `/api/employees/department/{departmentId}` | Path `departmentId` (Long), Auth MANAGER/ADMIN. | 200 array `EmployeeDTO`. | 404 department missing. | `curl /api/employees/department/9` |
| DELETE deactivateEmployee | Deactivate employee. | DELETE `/api/employees/{id}` | Path `id` (Long), Auth ADMIN. | 200 message success. | 400 errors. | `curl -X DELETE /api/employees/42` |
| PUT updateEmployeeRole | Update role. | PUT `/api/employees/{id}/role` | Path `id` (Long), Body {role (String, required)}, Auth ADMIN. | 200 updated `EmployeeDTO`. | 400 invalid role. | `curl -X PUT /api/employees/42/role -d '{"role":"MANAGER"}'` |
| POST createEmployee | Create employee with temp password. | POST `/api/employees` | Body Map {buildingId (Long, required), firstName?, lastName?, email?, phoneNumber?, dateOfBirth?, address?, emergency contacts?, role?}, Auth MANAGER/ADMIN. | 200 `EmployeeDTO` with generated credentials. | 400 missing building or invalid values. | `curl -X POST /api/employees -d '{"buildingId":1,"email":"new@staff.com"}'` |
| POST assignEmployeesToDepartment | Bulk assign employees. | POST `/api/employees/departments/{departmentId}/assign` | Path `departmentId` (Long), Body {employeeIds (List<Long>, required)}, Auth MANAGER/ADMIN. | 200 message success. | 400 missing IDs. | `curl -X POST /api/employees/departments/9/assign -d '{"employeeIds":[1,2]}'` |
| POST changePassword | Self-service password change. | POST `/api/employees/change-password` | Body {oldPassword? (String), newPassword (String)}, Auth principal. | 200 message. | 400 missing/invalid; 400 when old password incorrect. | `curl -X POST /api/employees/change-password -d '{"oldPassword":"old","newPassword":"NewPass!1"}'` |
| POST sendTempPasswordToEmployee | Force temp password email. | POST `/api/employees/{id}/send-temp-password` | Path `id` (Long), Body optional map, Auth MANAGER/ADMIN. | 200 message success. | 400 service errors. | `curl -X POST /api/employees/42/send-temp-password -d '{}'` |


#### DepartmentController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| GET getAllDepartments | List departments accessible to manager/admin. | GET `/api/departments` | Auth MANAGER/ADMIN. | 200 array `DepartmentDTO`. | 403 unauthorized. | `curl /api/departments` |
| GET getDepartment | Get department details. | GET `/api/departments/{id}` | Path `id` (Long). | 200 `DepartmentDTO`. | 403 when not allowed. | `curl /api/departments/5` |
| POST createDepartment | Create department in building. | POST `/api/departments` | Body Map {buildingId (Long, required), name (String), description?, minStaffing?, maxStaffing?, totalShifts?}, Auth MANAGER/ADMIN. | 200 `DepartmentDTO`. | 400 missing building; 403 forbidden; 400 on errors. | `curl -X POST /api/departments -d '{"buildingId":1,"name":"Front Desk"}'` |
| PUT updateDepartment | Update department metadata. | PUT `/api/departments/{id}` | Path `id` (Long), Body fields as above. | 200 `DepartmentDTO`. | 400/403 errors. | `curl -X PUT /api/departments/5 -d '{"description":"Updated"}'` |
| DELETE deleteDepartment | Remove department. | DELETE `/api/departments/{id}` | Path `id` (Long). | 200 success message. | 400/403 errors. | `curl -X DELETE /api/departments/5` |
| GET getDepartmentsByBuilding | List departments for building. | GET `/api/departments/by-building/{buildingId}` | Path `buildingId` (Long). | 200 array `DepartmentDTO`. | 403 unauthorized; 404 building missing. | `curl /api/departments/by-building/3` |

#### BuildingController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| POST createBuilding | Create building for admin and assign self. | POST `/api/buildings` | Body {name (String, required), address (String, required)}, Auth ADMIN. | 200 `BuildingDTO`. | 400 missing fields; 409 duplicate. | `curl -X POST /api/buildings -d '{"name":"HQ","address":"123 Main"}'` |
| GET getMyBuilding | List buildings managed by manager. | GET `/api/buildings/my-building` | Auth MANAGER. | 200 array `BuildingDTO`. | 404 when none assigned. | `curl /api/buildings/my-building` |
| GET getMyBuildings | List admin's buildings. | GET `/api/buildings/my-buildings` | Auth ADMIN. | 200 array `BuildingDTO`. | None. | `curl /api/buildings/my-buildings` |
| GET adminHasBuildings | Check if admin has buildings. | GET `/api/buildings/admin-has-buildings` | Auth ADMIN. | 200 `{hasBuildings:boolean}`. | None. | `curl /api/buildings/admin-has-buildings` |
| PUT addManagerToBuilding | Assign manager to building. | PUT `/api/buildings/{buildingId}/add-manager` | Path `buildingId` (Long), Body {managerId (Long, required)}. | 200 message with IDs. | 400 invalid managerId; 404 building/manager missing. | `curl -X PUT /api/buildings/1/add-manager -d '{"managerId":7}'` |
| PUT removeManagerFromBuilding | Remove manager from building. | PUT `/api/buildings/{buildingId}/remove-manager` | Path `buildingId` (Long), Body {managerId (Long, required)}. | 200 message success. | 400 invalid managerId; 404 not found. | `curl -X PUT /api/buildings/1/remove-manager -d '{"managerId":7}'` |

#### EmployeeAvailabilityController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| GET getAvailability | _documentation needed_ – returns availability blocks. | GET `/api/employees/{employeeId}/availability` | Path `employeeId` (Long). | 200 array of `EmployeeAvailability`. | None specified. | `curl /api/employees/42/availability` |
| POST setAvailability | Replace employee availability entries. | POST `/api/employees/{employeeId}/availability` | Path `employeeId` (Long), Body array of `EmployeeAvailability` JSON. | 200 empty body on success. | None specified. | `curl -X POST /api/employees/42/availability -d '[{...}]'` |

#### BulkEmployeeImportController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| POST importEmployeesCsv | Upload CSV to create multiple employees. | POST `/api/employees/import/csv` | Multipart file `file` (CSV, required); headers optional for auditing; Auth MANAGER/ADMIN. | 200 `BulkEmployeeImportResult` {total, success, failed, errors}. | 400 invalid file type/size/content. | `curl -X POST -F 'file=@employees.csv' /api/employees/import/csv` |

### Shift Operations

#### ShiftController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| POST cancelPostedShift | Cancel shift posted for pickup. | POST `/api/shifts/{id}/cancel-post` | Path `id` (Long), Auth principal. | 200 message. | 400 on failure. | `curl -X POST /api/shifts/10/cancel-post` |
| GET getIncomingTrades | List trades directed at employee. | GET `/api/shifts/trades/incoming` | Auth EMPLOYEE. | 200 array of `ShiftTradeResponse`. | 401 when unauthenticated. | `curl /api/shifts/trades/incoming` |
| POST acceptTrade | Employee accepts trade to await approval. | POST `/api/shifts/trades/{id}/accept` | Path `id` (Long). | 200 message. | 400 service errors. | `curl -X POST /api/shifts/trades/5/accept` |
| POST declineTrade | Employee declines trade. | POST `/api/shifts/trades/{id}/decline` | Path `id` (Long). | 200 message. | 400 service errors. | `curl -X POST /api/shifts/trades/5/decline` |
| GET getShifts | Retrieve shifts filtered by role or query. | GET `/api/shifts` | Query: employeeId?, departmentId?, startDate?, endDate?; Auth principal. | 200 array `ShiftResponse`. | None. | `curl '/api/shifts?departmentId=2'` |
| GET getMyShifts | Get current user's shifts. | GET `/api/shifts/my-shifts` | Query: startDate?, endDate?. | 200 array `ShiftResponse`. | None. | `curl /api/shifts/my-shifts` |
| GET getAvailableShifts | List shifts available for pickup. | GET `/api/shifts/available` | Auth principal. | 200 array `ShiftResponse`. | 400 on errors. | `curl /api/shifts/available` |
| GET getShift | Fetch shift by ID with access control. | GET `/api/shifts/{id}` | Path `id` (Long). | 200 `ShiftResponse`. | 403 unauthorized; 404 missing. | `curl /api/shifts/15` |
| POST createShift | Create shift. | POST `/api/shifts` | Body `CreateShiftRequest` {details _documentation needed_}, Auth MANAGER/ADMIN. | 200 `ShiftResponse`. | 400 validation errors. | `curl -X POST /api/shifts -d '{...}'` |
| PUT updateShift | Update shift. | PUT `/api/shifts/{id}` | Path `id` (Long), Body `CreateShiftRequest`. | 200 `ShiftResponse`. | 400 errors. | `curl -X PUT /api/shifts/15 -d '{...}'` |
| DELETE deleteShift | Delete shift. | DELETE `/api/shifts/{id}` | Path `id` (Long). | 200 message. | 400 errors. | `curl -X DELETE /api/shifts/15` |
| POST giveAwayShift | Mark shift for pickup with reason. | POST `/api/shifts/{id}/give-away` | Path `id` (Long), Body `ShiftTradeRequest` {reason (String optional)}, Auth principal. | 200 message. | 400 errors. | `curl -X POST /api/shifts/15/give-away -d '{"reason":"Need time off"}'` |
| POST pickupShift | Pick up shift. | POST `/api/shifts/{id}/pick-up` | Path `id` (Long), Auth principal. | 200 message. | 403 wrong department; 400 errors. | `curl -X POST /api/shifts/15/pick-up` |
| GET getShiftTrades | List trades visible to user. | GET `/api/shifts/trades` | Auth principal. | 200 array `ShiftTradeResponse`. | 400 errors. | `curl /api/shifts/trades` |
| POST pickupTrade | Placeholder endpoint to pick up trade. | POST `/api/shifts/trades/{id}/pickup` | Path `id` (Long). | 200 message. | 400 errors. | `curl -X POST /api/shifts/trades/5/pickup` |
| POST approveTrade | Approve pending trade. | POST `/api/shifts/trades/{id}/approve` | Path `id` (Long), Auth MANAGER/ADMIN. | 200 message. | 400 not found or wrong status. | `curl -X POST /api/shifts/trades/5/approve` |
| POST rejectTrade | Reject pending trade with optional reason. | POST `/api/shifts/trades/{id}/reject` | Path `id` (Long), Body {reason?}, Auth MANAGER/ADMIN. | 200 message. | 400 not found or wrong status. | `curl -X POST /api/shifts/trades/5/reject -d '{"reason":"Overstaffed"}'` |
| DELETE deleteTrade | Cancel trade. | DELETE `/api/shifts/trades/{id}` | Path `id` (Long), Auth principal. | 200 message. | 400 errors. | `curl -X DELETE /api/shifts/trades/5` |
| GET getShiftStatistics | Shift KPI statistics. | GET `/api/shifts/statistics` | Query: startDate?, endDate?, departmentId?. | 200 JSON metrics. | 400 errors. | `curl '/api/shifts/statistics?startDate=2024-01-01'` |
| GET getShiftAnalytics | Analytics summary. | GET `/api/shifts/analytics` | Query: startDate?, endDate?, departmentId?. | 200 JSON analytics. | 400 errors. | `curl /api/shifts/analytics` |
| GET getEmployeeHours | Employee hours report. | GET `/api/shifts/employee-hours` | Query: startDate?, endDate?, departmentId?; Auth principal. | 200 array of maps per employee. | 400 errors. | `curl /api/shifts/employee-hours` |
| GET getDepartmentStats | Department-level stats. | GET `/api/shifts/department-stats` | Query: startDate?, endDate?. | 200 array of maps. | 400 errors. | `curl /api/shifts/department-stats` |
| POST tradeShiftToEmployee | Offer shift to a specific employee. | POST `/api/shifts/{id}/trade` | Path `id` (Long), Body {targetEmployeeId (Long required)}, Auth principal. | 200 message. | 400 missing targetEmployeeId. | `curl -X POST /api/shifts/15/trade -d '{"targetEmployeeId":21}'` |
| POST postShiftToEveryone | Post shift to all eligible staff. | POST `/api/shifts/{id}/post-to-everyone` | Path `id` (Long), Auth principal. | 200 message. | 400 errors. | `curl -X POST /api/shifts/15/post-to-everyone` |

#### ShiftRequirementController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| GET getAll | _documentation needed_ – list all shift requirements. | GET `/api/shift-requirements` | None. | 200 array `ShiftRequirement`. | None specified. | `curl /api/shift-requirements` |
| GET getById | Retrieve shift requirement by ID. | GET `/api/shift-requirements/{id}` | Path `id` (Long). | 200 `ShiftRequirement`. | 404 when missing. | `curl /api/shift-requirements/5` |
| POST create | Create shift requirement. | POST `/api/shift-requirements` | Body `ShiftRequirement` entity JSON. | 200 created requirement. | None specified. | `curl -X POST /api/shift-requirements -d '{...}'` |
| PUT update | Update shift requirement. | PUT `/api/shift-requirements/{id}` | Path `id` (Long), Body `ShiftRequirement`. | 200 updated requirement. | 404 when missing. | `curl -X PUT /api/shift-requirements/5 -d '{...}'` |
| DELETE delete | Delete shift requirement. | DELETE `/api/shift-requirements/{id}` | Path `id` (Long). | 204 No Content. | 404 when missing. | `curl -X DELETE /api/shift-requirements/5` |
| GET getByDepartment | List requirements for department. | GET `/api/shift-requirements/department/{departmentId}` | Path `departmentId` (Long). | 200 array. | None. | `curl /api/shift-requirements/department/9` |
| GET getByDepartmentAndDateRange | List requirements within date range. | GET `/api/shift-requirements/department/{departmentId}/range` | Path `departmentId` (Long), Query `start` (LocalDate), `end` (LocalDate). | 200 array. | None specified. | `curl '/api/shift-requirements/department/9/range?start=2024-01-01&end=2024-01-31'` |

#### ShiftTemplateController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| GET getAllTemplates | _documentation needed_ – list all shift templates. | GET `/api/shift-templates` | None. | 200 array `ShiftTemplate`. | None. | `curl /api/shift-templates` |
| GET getTemplatesByDepartment | Templates by department. | GET `/api/shift-templates/department/{departmentId}` | Path `departmentId` (Long). | 200 array `ShiftTemplate`. | None. | `curl /api/shift-templates/department/9` |
| GET getTemplate | Fetch single template. | GET `/api/shift-templates/{id}` | Path `id` (Long). | 200 `ShiftTemplate`. | 404 when missing. | `curl /api/shift-templates/5` |
| POST createTemplate | Create template. | POST `/api/shift-templates` | Body `ShiftTemplate` JSON. | 200 created template. | None. | `curl -X POST /api/shift-templates -d '{...}'` |
| PUT updateTemplate | Update template. | PUT `/api/shift-templates/{id}` | Path `id` (Long), Body `ShiftTemplate`. | 200 updated template. | 404 when missing. | `curl -X PUT /api/shift-templates/5 -d '{...}'` |
| DELETE deleteTemplate | Delete template. | DELETE `/api/shift-templates/{id}` | Path `id` (Long). | 204 No Content. | 404 when missing. | `curl -X DELETE /api/shift-templates/5` |

#### AutoSchedulingController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| POST autoSchedule | Execute auto scheduling algorithm. | POST `/api/auto-scheduling` | Body `AutoScheduleRequestDTO` {_documentation needed_}. | 200 `AutoScheduleResultDTO`. | None specified. | `curl -X POST /api/auto-scheduling -d '{...}'` |

### Waitlist & Super Admin Management

#### WaitlistController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| POST joinWaitlist | Add email to public waitlist. | POST `/api/public/waitlist/join` | Body {email (String, required)}. | 200 message success; duplicates return friendly message. | 400 invalid email. | `curl -X POST /api/public/waitlist/join -d '{"email":"guest@example.com"}'` |
| GET getWaitlistCount | Public waitlist count. | GET `/api/public/waitlist/count` | None. | 200 `{count: number}`. | None. | `curl /api/public/waitlist/count` |

#### SuperAdminController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| POST setTempPasswordManual | Set admin temp password without email. | POST `/api/super-admin/admins/{id}/set-temp-password-manual` | Path `id` (Long), Body {tempPassword (String, required)}. | 200 message. | 400 missing tempPassword; 404 admin not found. | `curl -X POST /api/super-admin/admins/3/set-temp-password-manual -d '{"tempPassword":"Temp123!"}'` |
| POST sendTempPasswordToAdmin | Set admin temp password and email. | POST `/api/super-admin/admins/{id}/send-temp-password` | Path `id` (Long), Body {tempPassword (String, required)}. | 200 message. | 400 missing password; 404 admin not found. | `curl -X POST /api/super-admin/admins/3/send-temp-password -d '{"tempPassword":"Temp123!"}'` |
| POST sendInvoiceToAllAdmins | Send Stripe invoices to all admins. | POST `/api/super-admin/admins/invoice/send-all` | None. | 200 message with count. | None (errors logged). | `curl -X POST /api/super-admin/admins/invoice/send-all` |
| POST sendInvoiceToAdmin | Send invoice to specific admin. | POST `/api/super-admin/admins/invoice/send/{id}` | Path `id` (Long). | 200 message. | 404 admin missing. | `curl -X POST /api/super-admin/admins/invoice/send/3` |
| GET getAllAdmins | List admins. | GET `/api/super-admin/admins` | None. | 200 array `Employee`. | None. | `curl /api/super-admin/admins` |
| POST createAdmin | Create admin account. | POST `/api/super-admin/admins` | Body {firstName, lastName, email, password?, packageType?}. | 200 created admin. | None documented. | `curl -X POST /api/super-admin/admins -d '{"email":"owner@example.com","password":"Secure1!"}'` |
| PUT updateAdmin | Update admin profile. | PUT `/api/super-admin/admins/{id}` | Path `id` (Long), Body fields {firstName?, lastName?, email?, packageType?}. | 200 updated admin. | 404 admin missing. | `curl -X PUT /api/super-admin/admins/3 -d '{"packageType":"Pro"}'` |
| DELETE deleteAdmin | Delete admin. | DELETE `/api/super-admin/admins/{id}` | Path `id` (Long). | 200 message. | None documented. | `curl -X DELETE /api/super-admin/admins/3` |

#### SuperAdminWaitlistController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| GET getAllWaitlistEntries | List all waitlist entries. | GET `/api/super-admin/waitlist` | None (SUPER_ADMIN). | 200 array entries. | None. | `curl /api/super-admin/waitlist` |
| GET getWaitlistCount (admin) | Count waitlist entries. | GET `/api/super-admin/waitlist/count` | None. | 200 `{count}`. | None. | `curl /api/super-admin/waitlist/count` |

### Notifications & Audit

#### NotificationController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| GET getNotifications | Fetch notifications for user. | GET `/api/notifications` | Auth principal. | 200 array notifications. | 400 service errors. | `curl /api/notifications` |
| POST markAsRead | Mark notification read. | POST `/api/notifications/{id}/read` | Path `id` (Long). | 200 message. | 400 service errors. | `curl -X POST /api/notifications/5/read` |
| POST markAllAsRead | Mark all notifications read. | POST `/api/notifications/read-all` | None. | 200 message. | 400 service errors. | `curl -X POST /api/notifications/read-all` |
| DELETE deleteNotification | Delete notification. | DELETE `/api/notifications/{id}` | Path `id` (Long). | 200 message. | 400 service errors. | `curl -X DELETE /api/notifications/5` |
| GET getNotificationSettings | Retrieve notification settings. | GET `/api/notifications/settings` | None. | 200 settings map. | 400 errors. | `curl /api/notifications/settings` |
| POST updateNotificationSettings | Update notification preferences. | POST `/api/notifications/settings` | Body Map settings (JSON). | 200 message. | 400 errors. | `curl -X POST /api/notifications/settings -d '{"email":true}'` |

#### UserActionLogController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| GET getLogsByBuilding | Get logs filtered by building. | GET `/api/logs/building/{buildingId}` | Path `buildingId` (Long), Auth ADMIN. | 200 array `UserActionLog`. | None. | `curl /api/logs/building/2` |
| GET getLogsByUser | Get logs by user UUID. | GET `/api/logs/user/{userUuid}` | Path `userUuid` (String), Auth ADMIN. | 200 array `UserActionLog`. | None. | `curl /api/logs/user/uuid-123` |
| GET getLogsByRole | Get logs by role. | GET `/api/logs/role/{role}` | Path `role` (String), Auth ADMIN. | 200 array `UserActionLog`. | None. | `curl /api/logs/role/MANAGER` |
| GET getAllLogs | Get all logs. | GET `/api/logs` | Auth ADMIN. | 200 array `UserActionLog`. | None. | `curl /api/logs` |

### Reporting & Analytics

#### ReportsController
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| GET getStatistics | Shift statistics summary. | GET `/api/reports/statistics` | Query: startDate?, endDate?, departmentId?, employeeId?; Auth MANAGER/ADMIN. | 200 `ReportStatisticsDTO`. | None. | `curl /api/reports/statistics` |
| GET getShiftsByDay | Daily shift distribution. | GET `/api/reports/shifts-by-day` | Query: startDate?, endDate?, departmentId?, employeeId?. | 200 `ShiftsByDayDTO`. | None. | `curl /api/reports/shifts-by-day` |
| GET getHoursByDepartment | Departmental hours summary. | GET `/api/reports/hours-by-department` | Query: startDate?, endDate? | 200 `HoursByDepartmentDTO`. | None. | `curl /api/reports/hours-by-department` |
| GET getShiftDistribution | Shift distribution stats. | GET `/api/reports/shift-distribution` | Query: startDate?, endDate?, departmentId? | 200 `ShiftDistributionDTO`. | None. | `curl /api/reports/shift-distribution` |
| GET getEmployeeHours | Employee hours detail. | GET `/api/reports/employee-hours` | Query: startDate?, endDate?, departmentId?, employeeId? | 200 List `EmployeeHoursDTO`. | None. | `curl /api/reports/employee-hours` |
| GET getDepartmentPerformance | Department performance metrics. | GET `/api/reports/department-performance` | Query: startDate?, endDate? | 200 List `DepartmentPerformanceDTO`. | None. | `curl /api/reports/department-performance` |
| GET getMonthlyTrend | Monthly trend data. | GET `/api/reports/monthly-trend` | Query: year?, departmentId? | 200 `MonthlyTrendDTO`. | None. | `curl /api/reports/monthly-trend` |

### Application Utilities

#### UseShiftlyApplication
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| init() | Initialize default timezone to America/Chicago after bean construction. | Method `UseShiftlyApplication.init()` | None. | void. | None. | Invoke automatically on startup. |
| main(String[] args) | Bootstraps Spring application. | Method `UseShiftlyApplication.main(String[] args)` | `args` (String[], optional). | void (starts application). | Propagates runtime errors. | `java -jar app.jar` |

#### BcryptGen
| Name / Signature | Description | Path | Parameters | Response | Errors | Example |
| --- | --- | --- | --- | --- | --- | --- |
| main(String[] args) | Utility to print BCrypt hash for sample password. | Method `BcryptGen.main(String[] args)` | None (password hardcoded in source). | Prints hash to stdout. | None. | `java com.useshiftly.scheduler.BcryptGen` |

### Configuration

#### application-prod.yml
| Property | Description | Default / Source | Notes |
| --- | --- | --- | --- |
| `server.port` | HTTP port for production server. | 8080 | Override via env if needed. |
| `spring.datasource.*` | PostgreSQL connection URL, username, password, driver. | Uses `${DB_USERNAME}`, `${DB_PASSWORD}`. | Set secrets via environment. |
| `spring.jpa` | Hibernate settings (ddl-auto none, SQL logging disabled, timezone). | Fixed values. | Ensures Flyway controls schema. |
| `spring.mail.*` | SMTP configuration. | Defaults to Gmail host/port. | Credentials pulled from env. |
| `spring.flyway.*` | Migration config (enabled, locations, baseline). | Enabled with baseline on migrate. | Validations disabled. |
| `app.jwt.secret` | JWT signing secret. | `${JWT_SECRET}` or default fallback. | Keep secret in env. |
| `app.jwt.expiration` | Token expiry (ms). | 86400000. | 24 hours. |
| `app.cors.allowed-origins` | Allowed CORS origins. | `${ALLOWED_ORIGINS}` default `https://scheduler.asluxeco.org`. | CSV string. |
| `app.notification.*` | Email notifications base URL and sender toggles. | Env overrides for `EMAIL_FROM`, `EMAIL_ENABLED`, `NOTIFICATION_BASE_URL`. | Controls email features. |
| `security.rate-limit.*` | Rate limiting policy (requests per minute, burst, block duration). | Uses env defaults `RATE_LIMIT_*`. | Tune for abuse prevention. |
| `security.abuse.*` | Abuse detection thresholds and durations. | Env overrides for thresholds. | Works with AbuseDetectionService. |
| `security.monitoring.*` | Alert thresholds/time windows. | Env defaults `SECURITY_ALERT_*`. | Drives security alerts. |
| `security.enhanced-logging.*` | Toggle structured security logging. | `${ENHANCED_LOGGING}` default true. | Adjust log verbosity. |
| `logging.*` | File name, log levels, console pattern. | Set to `logs/useshiftly.log`, INFO levels. | Customize via env if needed. |

### Example Usage Snippet
```java
// Refresh JWT for current user in a controller/service
ResponseEntity<?> response = authController.refreshToken(authenticatedEmployee);
if (response.getStatusCode().is2xxSuccessful()) {
    String json = (String) response.getBody();
    // parse token string as needed
}
```

<!-- API-REFERENCE-END -->
