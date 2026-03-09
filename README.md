# Student Activity & Academic Tracking System

A comprehensive full-stack web application for B.Tech colleges to manage student activities, attendance tracking, performance monitoring, and analytics.

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MongoDB 6.x
- **Authentication**: JWT (JSON Web Tokens)
- **Security**: Spring Security with RBAC
- **Build Tool**: Maven

### Frontend
- **Framework**: React 18.2
- **Build Tool**: Vite
- **Routing**: React Router v6
- **HTTP Client**: Axios
- **Charts**: Recharts
- **Forms**: Formik + Yup
- **Notifications**: React Toastify

## Features

### User Roles
- **Admin**: Complete system management (students, faculty, courses, subjects)
- **Faculty**: Attendance marking, performance management, report generation
- **Student**: View attendance, performance, and analytics

### Core Functionality
- JWT-based authentication with access and refresh tokens
- Role-based access control (RBAC)
- Student and faculty management
- Course and subject management
- Attendance tracking with bulk operations
- Performance management with auto-grade calculation
- Rich data visualizations using Recharts
- Notification system
- Audit logging
- Report generation (PDF/CSV export)
- File upload (Excel bulk import, profile images)
- Advanced search, filtering, and pagination

## Project Structure

```
.
├── backend/                    # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/college/activitytracker/
│   │   │   │   ├── config/           # Configuration classes
│   │   │   │   ├── security/         # JWT and security components
│   │   │   │   ├── controller/       # REST API controllers
│   │   │   │   ├── service/          # Business logic services
│   │   │   │   ├── repository/       # MongoDB repositories
│   │   │   │   ├── model/            # Entity models
│   │   │   │   ├── dto/              # Data Transfer Objects
│   │   │   │   ├── exception/        # Exception handling
│   │   │   │   └── util/             # Utility classes
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/                     # Unit and integration tests
│   └── pom.xml
│
├── frontend/                   # React frontend
│   ├── src/
│   │   ├── api/                      # API configuration and endpoints
│   │   ├── components/               # Reusable components
│   │   │   ├── common/
│   │   │   ├── charts/
│   │   │   └── layout/
│   │   ├── pages/                    # Page components
│   │   │   ├── admin/
│   │   │   ├── faculty/
│   │   │   ├── student/
│   │   │   └── auth/
│   │   ├── context/                  # React Context (Auth)
│   │   ├── routes/                   # Routing configuration
│   │   ├── utils/                    # Utility functions
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
│
└── .kiro/specs/student-activity-tracking-system/
    ├── requirements.md         # Detailed requirements
    ├── design.md              # System design document
    └── tasks.md               # Implementation tasks
```

## Prerequisites

### Backend
- Java 17 or higher
- Maven 3.8+
- MongoDB 6.x (local or MongoDB Atlas)

### Frontend
- Node.js 18+ and npm

## Installation & Setup

### 1. MongoDB Setup

**Option A: Local MongoDB**
```bash
# Install MongoDB 6.x
# Start MongoDB service
mongod --dbpath /path/to/data
```

**Option B: MongoDB Atlas (Cloud)**
1. Create a free account at [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create a new cluster
3. Get your connection string
4. Update `backend/src/main/resources/application.properties`

### 2. Backend Setup

```bash
cd backend

# Update application.properties with your MongoDB connection string
# Edit: src/main/resources/application.properties

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Backend will start on http://localhost:8080
```

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Create .env file for environment variables
echo "VITE_API_URL=http://localhost:8080/api/v1" > .env

# Start development server
npm run dev

# Frontend will start on http://localhost:3000
```

## Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/student_tracker

# JWT Secret (Change in production!)
jwt.secret=your-256-bit-secret-key-change-this-in-production

# JWT Expiration (in milliseconds)
jwt.access-token-expiration=3600000        # 1 hour
jwt.refresh-token-expiration=604800000     # 7 days

# CORS
cors.allowed-origins=http://localhost:3000,http://localhost:5173

# File Upload
file.upload-dir=./uploads
spring.servlet.multipart.max-file-size=10MB
```

### Frontend Configuration

Create `frontend/.env`:

```env
VITE_API_URL=http://localhost:8080/api/v1
```

## Running Tests

### Backend Tests
```bash
cd backend

# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AuthServiceTest
```

### Frontend Tests
```bash
cd frontend

# Run tests
npm test

# Run tests with coverage
npm test -- --coverage
```

## API Documentation

Once the backend is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

API documentation is also available at:
```
http://localhost:8080/api-docs
```

## Default Users

After initial setup, you can create an admin user through the registration endpoint or directly in MongoDB.

Example admin user:
```json
{
  "email": "admin@college.edu",
  "password": "Admin@123",
  "role": "ROLE_ADMIN"
}
```

## Development Workflow

1. **Backend Development**:
   - Create models in `model/` package
   - Create repositories in `repository/` package
   - Implement business logic in `service/` package
   - Create REST endpoints in `controller/` package
   - Write tests in `src/test/`

2. **Frontend Development**:
   - Create API endpoints in `src/api/endpoints/`
   - Build reusable components in `src/components/`
   - Create page components in `src/pages/`
   - Add routes in `src/routes/AppRoutes.jsx`

## Building for Production

### Backend
```bash
cd backend
mvn clean package
java -jar target/activity-tracker-1.0.0.jar
```

### Frontend
```bash
cd frontend
npm run build
# Build output will be in dist/ folder
```

## Deployment

See the [Design Document](.kiro/specs/student-activity-tracking-system/design.md) for detailed deployment instructions including:
- MongoDB Atlas setup
- Backend deployment (AWS EC2, Render, Railway)
- Frontend deployment (Vercel, Netlify)
- CI/CD pipeline setup

## Project Documentation

- **Requirements**: `.kiro/specs/student-activity-tracking-system/requirements.md`
- **Design**: `.kiro/specs/student-activity-tracking-system/design.md`
- **Tasks**: `.kiro/specs/student-activity-tracking-system/tasks.md`

## Contributing

1. Follow the task list in `tasks.md`
2. Write tests for all new features
3. Follow the coding standards defined in the design document
4. Update documentation as needed

## Security Notes

⚠️ **Important for Production**:
1. Change the JWT secret key in `application.properties`
2. Use environment variables for sensitive configuration
3. Enable HTTPS
4. Configure proper CORS origins
5. Set up rate limiting
6. Enable MongoDB authentication
7. Regular security audits

## License

This project is developed for educational purposes.

## Support

For issues and questions, please refer to the project documentation or create an issue in the repository.
