#!/bin/bash
# Test login endpoint directly

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@college.edu","password":"Admin@123"}' \
  -v
