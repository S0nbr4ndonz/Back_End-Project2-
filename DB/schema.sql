-- Code that creates the database schema for the project

-- 1. users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(20),
    oauth_provider VARCHAR(20),
    oauth_subject VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE
);

-- 2. job_entries
CREATE TABLE job_entries (
    job_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    company VARCHAR(100) NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    salary_text VARCHAR(100),

    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- 3. job_applications
CREATE TABLE job_applications (
    application_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    job_id INTEGER NOT NULL,
    status VARCHAR(50),
    date_applied DATE DEFAULT CURRENT_DATE,

    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    FOREIGN KEY (job_id)
        REFERENCES job_entries(job_id)
        ON DELETE CASCADE,

    UNIQUE (user_id, job_id)
);

-- 4. application_notes
CREATE TABLE application_notes (
    notes_id SERIAL PRIMARY KEY,
    application_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    last_edited TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (application_id)
        REFERENCES job_applications(application_id)
        ON DELETE CASCADE
);