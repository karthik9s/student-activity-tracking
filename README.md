# Student Activity & Academic Tracking System

A full-stack web application for B.Tech colleges to manage student activities, attendance tracking, performance monitoring, and analytics.

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MongoDB (Atlas or local)
- **Authentication**: JWT (access + refresh tokens)
- **Security**: Spring Security with RBAC
- **Build Tool**: Maven

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite
- **Routing**: React Router v6
- **HTTP Client**: Axios
- **Charts**: Recharts
- **Notifications**: React Toastify

## Features

- JWT-based authentication with role-based access control
- **Admin**: Manage students, faculty, courses, subjects, allocations
- **Faculty**: Mark attendance (bulk), enter performance/marks, generate reports
- **Student**: View attendance, performance, and analytics
- Auto grade calculation from marks
- Report generation with PDF/CSV export
- Audit logging and notification system
- Rate limiting and brute-force protection

## Project Structure

```
.
├── backend/                    # Spring Boot backend
│   └── src/main/java/com/college/activitytracker/
│       ├── config/             # CORS, Security, MongoDB config
│       ├── controller/         # REST API controllers
│       ├── service/            # Business logic
│       ├── repository/         # MongoDB repositories
│       ├── model/              # Entity models
│       ├── dto/                # Data Transfer Objects
│       └── exception/          # Global exception handling
│
└── frontend/                   # React frontend
    └── src/
        ├── api/                # Axios config and endpoint functions
        ├── components/         # Reusable UI components
        ├── pages/              # admin/, faculty/, student/, auth/
        ├── context/            # AuthContext
        └── routes/             # AppRoutes
```

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+ and npm
- MongoDB (local or [MongoDB Atlas](https://www.mongodb.com/cloud/atlas))

## Setup

### 1. Backend

```bash
cd backend

# Update MongoDB URI and JWT secret in:
# src/main/resources/application.properties

mvn spring-boot:run
# Starts on http://localhost:8080
```

### 2. Frontend

```bash
cd frontend

npm install

# Create .env file
echo "VITE_API_URL=http://localhost:8080/api/v1" > .env

npm run dev
# Starts on http://localhost:5173
```

## Configuration

`backend/src/main/resources/application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/student_tracker
jwt.secret=your-256-bit-secret-key-change-this-in-production
jwt.access-token-expiration=3600000
jwt.refresh-token-expiration=604800000
cors.allowed-origins=http://localhost:3000,http://localhost:5173
```

`frontend/.env`:

```env
VITE_API_URL=http://localhost:8080/api/v1
```

## API Documentation

With the backend running, Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```

## Building for Production

```bash
# Backend
cd backend && mvn clean package
java -jar target/activity-tracker-1.0.0.jar

# Frontend
cd frontend && npm run build
# Output in dist/
```

## Security Notes

Before deploying to production:
1. Change the JWT secret key — never commit it to source control
2. Use environment variables for all sensitive config
3. Enable HTTPS
4. Set correct CORS origins
5. Enable MongoDB authentication

## License

Developed for educational purposes.
