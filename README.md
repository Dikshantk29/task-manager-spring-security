# Task Manager API 🛡️

A secure REST API built with Spring Boot and JWT Authentication.

## Tech Stack
- Java 17
- Spring Boot 3.x
- Spring Security
- JWT (JSON Web Tokens)
- MySQL
- Spring Data JPA
- Lombok

## Features
- JWT Authentication
- Role-based Authorization (USER / ADMIN)
- Users can only access their own tasks
- BCrypt password hashing
- Stateless REST API

## API Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | /auth/register | Public | Register new user |
| POST | /auth/login | Public | Login and get JWT token |
| POST | /tasks | USER | Create a task |
| GET | /tasks | USER | Get your tasks |
| PUT | /tasks/{id} | USER | Update your task |
| DELETE | /tasks/{id} | USER | Delete your task |
| GET | /tasks/all | ADMIN | Get all tasks |

## How to Run
1. Clone the repository
2. Create MySQL database: `CREATE DATABASE taskmanager_db;`
3. Update `application.properties` with your MySQL credentials
4. Run the application
5. Test APIs using Postman

## Security Flow
```
POST /auth/login → JWT Token
Every request → Authorization: Bearer <token>
JwtFilter validates token → sets user in SecurityContext
```