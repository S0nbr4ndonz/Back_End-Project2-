TRUNCATE TABLE application_notes, job_applications, job_entries, users
RESTART IDENTITY CASCADE;

-- =========================
-- 1) USERS
-- 1 admin, 2 normal users
-- =========================
INSERT INTO users (username, email, role, oauth_provider, oauth_subject)
VALUES
    ('admin1', 'admin1@example.com', 'ADMIN', 'google', 'admin-sub-001'),
    ('user1', 'user1@example.com', 'USER', 'google', 'user1-sub-001'),
    ('user2', 'user2@example.com', 'USER', 'github', 'user2-sub-001');

-- =========================
-- 2) JOB ENTRIES
-- user1 -> 5 jobs
-- user2 -> 5 jobs
-- admin -> 0 jobs
-- =========================
INSERT INTO job_entries (user_id, company, job_title, salary_text, posting_url)
VALUES
-- user1 jobs
(2, 'Google',   'Software Engineer Intern', '$45/hr', 'https://example.com/google-se-intern'),
(2, 'Amazon',   'Data Analyst Intern',      '$42/hr', 'https://example.com/amazon-da-intern'),
(2, 'Meta',     'Backend Developer Intern', '$48/hr', 'https://example.com/meta-backend-intern'),
(2, 'Apple',    'Machine Learning Intern',  '$50/hr', 'https://example.com/apple-ml-intern'),
(2, 'Netflix',  'Data Engineer Intern',     '$46/hr', 'https://example.com/netflix-de-intern'),

-- user2 jobs
(3, 'Tesla',    'Software Engineer Intern', '$44/hr', 'https://example.com/tesla-se-intern'),
(3, 'NVIDIA',   'Data Scientist Intern',    '$47/hr', 'https://example.com/nvidia-ds-intern'),
(3, 'Adobe',    'Frontend Developer Intern','$41/hr', 'https://example.com/adobe-fe-intern'),
(3, 'Uber',     'Backend Engineer Intern',  '$43/hr', 'https://example.com/uber-be-intern'),
(3, 'LinkedIn', 'Product Analyst Intern',   '$40/hr', 'https://example.com/linkedin-pa-intern');

-- =========================
-- 3) JOB APPLICATIONS
-- one application for each job entry
-- =========================
INSERT INTO job_applications (user_id, job_id, status, date_applied)
VALUES
-- user1 applications
(2, 1, 'APPLIED',   '2026-03-01'),
(2, 2, 'INTERVIEW', '2026-03-02'),
(2, 3, 'APPLIED',   '2026-03-03'),
(2, 4, 'REJECTED',  '2026-03-04'),
(2, 5, 'OFFER',     '2026-03-05'),

-- user2 applications
(3, 6, 'APPLIED',   '2026-03-01'),
(3, 7, 'INTERVIEW', '2026-03-02'),
(3, 8, 'APPLIED',   '2026-03-03'),
(3, 9, 'REJECTED',  '2026-03-04'),
(3, 10, 'APPLIED',  '2026-03-05');

-- =========================
-- 4) APPLICATION NOTES
-- two notes for each normal user
-- total = 4 notes
-- =========================
INSERT INTO application_notes (application_id, content, last_edited)
VALUES
    (1, 'Need to follow up with recruiter next week.', '2026-03-06 10:00:00'),
    (3, 'Referral submitted by a classmate.',          '2026-03-06 11:00:00'),
    (6, 'Customized resume submitted for this role.',  '2026-03-06 12:00:00'),
    (8, 'Prepare portfolio before next interview.',    '2026-03-06 13:00:00');
