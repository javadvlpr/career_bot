-- Bot state enum constraint removal
ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS users_bot_state_check;
ALTER TABLE IF EXISTS companies DROP CONSTRAINT IF EXISTS companies_bot_state_check;

-- ==================== CATEGORIES ====================
INSERT INTO categories (id, name) VALUES (1, 'IT') ON CONFLICT DO NOTHING;
INSERT INTO categories (id, name) VALUES (2, 'Marketing') ON CONFLICT DO NOTHING;
INSERT INTO categories (id, name) VALUES (3, 'Accounting') ON CONFLICT DO NOTHING;
INSERT INTO categories (id, name) VALUES (4, 'Education') ON CONFLICT DO NOTHING;
INSERT INTO categories (id, name) VALUES (5, 'Building') ON CONFLICT DO NOTHING;
INSERT INTO categories (id, name) VALUES (6, 'Medicine') ON CONFLICT DO NOTHING;
INSERT INTO categories (id, name) VALUES (7, 'Design') ON CONFLICT DO NOTHING;
INSERT INTO categories (id, name) VALUES (8, 'Sales') ON CONFLICT DO NOTHING;

-- ==================== PROFESSIONS — IT ====================
INSERT INTO professions (id, name, category_id) VALUES (1, 'Java Developer', 1) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (2, 'Python Developer', 1) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (3, 'Frontend Developer', 1) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (4, 'Backend Developer', 1) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (5, 'Mobile Developer', 1) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (6, 'DevOps Engineer', 1) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (7, 'QA Engineer', 1) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (8, 'Data Analyst', 1) ON CONFLICT DO NOTHING;

-- ==================== PROFESSIONS — Marketing ====================
INSERT INTO professions (id, name, category_id) VALUES (9, 'SMM Manager', 2) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (10, 'SEO Specialist', 2) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (11, 'Marketing Manager', 2) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (12, 'Content Creator', 2) ON CONFLICT DO NOTHING;

-- ==================== PROFESSIONS — Accounting ====================
INSERT INTO professions (id, name, category_id) VALUES (13, 'Chief accountant', 3) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (14, 'Accountant', 3) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (15, 'Auditor', 3) ON CONFLICT DO NOTHING;

-- ==================== PROFESSIONS — Education ====================
INSERT INTO professions (id, name, category_id) VALUES (16, 'English teacher', 4) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (17, 'Mathematics teacher', 4) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (18, 'IT teacher', 4) ON CONFLICT DO NOTHING;

-- ==================== PROFESSIONS — Building ====================
INSERT INTO professions (id, name, category_id) VALUES (19, 'Architect', 5) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (20, 'Project engineer', 5) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (21, 'Building worker(master)', 5) ON CONFLICT DO NOTHING;

-- ==================== PROFESSIONS — Medicine ====================
INSERT INTO professions (id, name, category_id) VALUES (22, 'Therapist', 6) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (23, 'Surgeon', 6) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (24, 'Dentist', 6) ON CONFLICT DO NOTHING;

-- ==================== PROFESSIONS — Design ====================
INSERT INTO professions (id, name, category_id) VALUES (25, 'UI/UX Designer', 7) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (26, 'Graphic designer', 7) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (27, 'Motion Designer', 7) ON CONFLICT DO NOTHING;

-- ==================== PROFESSIONS — Sales ====================
INSERT INTO professions (id, name, category_id) VALUES (28, 'Seller', 8) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (29, 'Sales Manager', 8) ON CONFLICT DO NOTHING;
INSERT INTO professions (id, name, category_id) VALUES (30, 'B2B Sales', 8) ON CONFLICT DO NOTHING;

-- ==================== SKILLS — IT ====================
INSERT INTO skills (id, name, category_id) VALUES (1, 'Java', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (2, 'Spring Boot', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (3, 'Python', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (4, 'Django', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (5, 'JavaScript', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (6, 'React', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (7, 'PostgreSQL', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (8, 'Docker', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (9, 'Git', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (10, 'Node.js', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (11, 'Kubernetes', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (12, 'Selenium', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (13, 'Flutter', 1) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (14, 'Vue.js', 1) ON CONFLICT DO NOTHING;

-- ==================== SKILLS — Marketing ====================
INSERT INTO skills (id, name, category_id) VALUES (15, 'Instagram Ads', 2) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (16, 'Facebook Ads', 2) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (17, 'Google Ads', 2) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (18, 'SEO', 2) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (19, 'Google Analytics', 2) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (20, 'Content Marketing', 2) ON CONFLICT DO NOTHING;

-- ==================== SKILLS — Accounting ====================
INSERT INTO skills (id, name, category_id) VALUES (21, '1C', 3) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (22, 'Excel', 3) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (23, 'Tax report', 3) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (24, 'Audit', 3) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (25, 'IFRS', 3) ON CONFLICT DO NOTHING;

-- ==================== SKILLS — Education ====================
INSERT INTO skills (id, name, category_id) VALUES (26, 'English', 4) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (27, 'Maths', 4) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (28, 'Pedagogy', 4) ON CONFLICT DO NOTHING;

-- ==================== SKILLS — Building ====================
INSERT INTO skills (id, name, category_id) VALUES (29, 'AutoCAD', 5) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (30, 'Design', 5) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (31, 'Estimate', 5) ON CONFLICT DO NOTHING;

-- ==================== SKILLS — Medicine ====================
INSERT INTO skills (id, name, category_id) VALUES (32, 'Therapy', 6) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (33, 'Surgery', 6) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (34, 'Dentistry', 6) ON CONFLICT DO NOTHING;

-- ==================== SKILLS — Design ====================
INSERT INTO skills (id, name, category_id) VALUES (35, 'Photoshop', 7) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (36, 'Figma', 7) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (37, 'Illustrator', 7) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (38, 'Adobe XD', 7) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (39, 'After Effects', 7) ON CONFLICT DO NOTHING;

-- ==================== SKILLS — Sales ====================
INSERT INTO skills (id, name, category_id) VALUES (40, 'CRM', 8) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (41, 'Negotiations', 8) ON CONFLICT DO NOTHING;
INSERT INTO skills (id, name, category_id) VALUES (42, 'B2B Sales', 8) ON CONFLICT DO NOTHING;

-- ==================== LOCATIONS ====================
INSERT INTO locations (id, name, is_active) VALUES (1, 'Tashkent', true) ON CONFLICT DO NOTHING;
INSERT INTO locations (id, name, is_active) VALUES (2, 'Samarkand', true) ON CONFLICT DO NOTHING;
INSERT INTO locations (id, name, is_active) VALUES (3, 'Bukhara', true) ON CONFLICT DO NOTHING;
INSERT INTO locations (id, name, is_active) VALUES (4, 'Namangan', true) ON CONFLICT DO NOTHING;
INSERT INTO locations (id, name, is_active) VALUES (5, 'Fergana', true) ON CONFLICT DO NOTHING;
INSERT INTO locations (id, name, is_active) VALUES (6, 'Andijan', true) ON CONFLICT DO NOTHING;
INSERT INTO locations (id, name, is_active) VALUES (7, 'Remote', true) ON CONFLICT DO NOTHING;

-- ==================== SEQUENCE SYNC ====================
-- After manual INSERT/DELETE, sequences must be advanced past max(id)
-- to prevent duplicate-key errors on next auto-generated insert.
SELECT setval('categories_id_seq',  COALESCE((SELECT MAX(id) FROM categories),  1));
SELECT setval('professions_id_seq', COALESCE((SELECT MAX(id) FROM professions), 1));
SELECT setval('skills_id_seq',      COALESCE((SELECT MAX(id) FROM skills),      1));
SELECT setval('locations_id_seq',   COALESCE((SELECT MAX(id) FROM locations),   1));