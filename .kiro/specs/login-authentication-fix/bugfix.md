# Bugfix Requirements Document

## Introduction

This document specifies the requirements for fixing a critical authentication bug where all login attempts return 401 Unauthorized, preventing users from accessing the application. The bug affects all authentication endpoints, including public routes that should be accessible without authentication. Investigation has confirmed that the database contains valid users with correct password hashes, and the issue appears to be in the Spring Security filter chain configuration where requests are rejected before reaching the AuthController.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a user attempts to login with valid credentials (admin@cvr.ac.in / admin123) THEN the system returns 401 Unauthorized with an empty response body

1.2 WHEN a user attempts to login with any valid credentials from the database THEN the system returns 401 Unauthorized without reaching the AuthController

1.3 WHEN a request is made to public auth endpoints like /api/v1/auth/logout THEN the system returns 401 Unauthorized despite the endpoint being configured as permitAll

1.4 WHEN authentication fails THEN the system returns an empty response body with no error message or details

1.5 WHEN a login attempt is made THEN the system produces no error logs in the backend console indicating the request never reaches the controller layer

### Expected Behavior (Correct)

2.1 WHEN a user attempts to login with valid credentials (admin@cvr.ac.in / admin123) THEN the system SHALL return 200 OK with JWT access and refresh tokens

2.2 WHEN a user attempts to login with valid credentials from the database THEN the system SHALL authenticate successfully and return appropriate tokens

2.3 WHEN a request is made to public auth endpoints like /api/v1/auth/login, /api/v1/auth/register, or /api/v1/auth/logout THEN the system SHALL allow the request to reach the controller without authentication

2.4 WHEN authentication fails due to invalid credentials THEN the system SHALL return 401 Unauthorized with a proper error message in the response body explaining the failure reason

2.5 WHEN a login attempt is made THEN the system SHALL log the request processing and any authentication errors for debugging purposes

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a user with valid JWT token accesses protected endpoints THEN the system SHALL CONTINUE TO validate the token and authorize access based on roles

3.2 WHEN password verification is performed THEN the system SHALL CONTINUE TO use BCrypt to compare provided passwords with stored hashes

3.3 WHEN the SecurityFilterChain processes requests to non-auth endpoints THEN the system SHALL CONTINUE TO require authentication for protected resources

3.4 WHEN CORS configuration is applied THEN the system SHALL CONTINUE TO allow cross-origin requests from the configured frontend origin

3.5 WHEN role-based access control is enforced THEN the system SHALL CONTINUE TO restrict endpoints based on user roles (ADMIN, FACULTY, STUDENT)

3.6 WHEN the database contains 29 valid users with correct password hashes THEN the system SHALL CONTINUE TO maintain data integrity and user records
