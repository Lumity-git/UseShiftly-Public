# UseShiftly Shift Scheduler

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
- `ALLOWED_ORIGINS` - Comma-separated list of allowed CORS origins (default: `https://scheduler.asluxeco.org`)
- `NOTIFICATION_BASE_URL` - Base URL for notification links (default: `https://useshiftly.com`)
- `EMAIL_FROM` - Sender email address (default: `noreply@useshiftly.com`)
- `EMAIL_ENABLED` - Enable/disable email notifications (default: `true`)
- `MAIL_HOST` - SMTP server host (default: `smtp.gmail.com`)
- `MAIL_PORT` - SMTP server port (default: `587`)
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password
- `RATE_LIMIT_RPM` - Rate limit requests per minute (default: `60`)
- `ABUSE_DETECTION_ENABLED` - Enable abuse detection (default: `true`)
- `ENHANCED_LOGGING` - Enable structured security logging (default: `true`)

### Application Profiles
- `dev` - Development profile (enables DataInitializer for sample data)
- `prod` - Production profile (uses Flyway migrations only)

   ```bash
   git clone <repository>

   spring:
       url: jdbc:postgresql://localhost:5432/useshiftly
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
The system creates these default users on first run:
- **Admin**: `admin@useshiftly.com` / `admin123`
- **Manager**: `manager@useshiftly.com` / `manager123`
- **Employee**: `employee@useshiftly.com` / `employee123`

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
  -d '{"email":"manager@useshiftly.com","password":"manager123"}'
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
   docker build -t useshiftly .
   docker run -p 8080:8080 useshiftly
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

Proprietary - Built for UseShiftly

---

**This is a production-ready application designed specifically for shift scheduling. It replaces manual processes with automated, digital solutions that improve efficiency and employee satisfaction.**
