# Back_End-Project2-
BackEnd repo for Project 2

##  Overview
This project is the **backend service** for the Job Tracking application. It is built using **Spring Boot** and provides a RESTful API that supports authentication, user management, job entries, job applications, and application notes.

The backend is responsible for handling business logic, database interactions, and secure communication with the frontend application.

---

## Team

Brandon Hernandez-Cano

Raphael Berjaoui

Rigoberto Rayon

Sungmin Park

---

## Tech Stack

- Java

- SpringBoot

- Spring Web MVC

- Spring Data JPA

- Spring Security

- OAuth2

- PostgreSQL

- OpenAPI/Swagger

- Maven

- JUnit testing

- Rest-assured

--- 

## Run locally



## Live API Url

[https://railway.com/project/ed994080-8b63-4da3-a108-d5aae67fa5d4?environmentId=0247b1cc-505b-4fde-9b26-183c0422b42c
](url)

--- 
##  Purpose
The purpose of this backend is to:

- Provide a REST API for managing job tracking data
- Handle OAuth-based user authentication
- Store and manage users, job entries, job applications, and notes
- Enforce security and access control
- Serve as the core logic layer of the application

---

##  Features

###  Authentication
- OAuth2 login support
- Automatic user creation on first login
- Session-based authentication

###  User Management
- Retrieve all users (admin)
- Retrieve user by ID
- Update user roles
- Delete users

###  Job Entries
- Create job entries
- Retrieve user-specific job entries
- Update and delete job entries

###  Job Applications
- Track job applications tied to job entries
- Full CRUD support (Create, Read, Update, Delete)

### 📝 Application Notes
- Add notes to job applications
- Update and delete notes
- Retrieve notes by application
