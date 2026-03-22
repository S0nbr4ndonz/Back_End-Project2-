# Back_End-Project2-
BackEnd repo for Project 2

## 📌 Overview
This project is the **backend service** for the Job Tracking application. It is built using **Spring Boot** and provides a RESTful API that supports authentication, user management, job entries, job applications, and application notes.

The backend is responsible for handling business logic, database interactions, and secure communication with the frontend application.

---

## 🎯 Purpose
The purpose of this backend is to:

- Provide a REST API for managing job tracking data
- Handle OAuth-based user authentication
- Store and manage users, job entries, job applications, and notes
- Enforce security and access control
- Serve as the core logic layer of the application

---

## 🚀 Features

### 🔐 Authentication
- OAuth2 login support
- Automatic user creation on first login
- Session-based authentication

### 👤 User Management
- Retrieve all users (admin)
- Retrieve user by ID
- Update user roles
- Delete users

### 💼 Job Entries
- Create job entries
- Retrieve user-specific job entries
- Update and delete job entries

### 📄 Job Applications
- Track job applications tied to job entries
- Full CRUD support (Create, Read, Update, Delete)

### 📝 Application Notes
- Add notes to job applications
- Update and delete notes
- Retrieve notes by application
