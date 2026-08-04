-- ============================================================
-- TestForge - Sample (dummy) data for all 9 tables
-- ============================================================
-- Run this AFTER TestForge_schema.sql has created the tables.
--
-- LOGIN DETAILS for every user in this file:
--     password:  password123
-- (stored as a real BCrypt hash, which Spring Security accepts)
--
-- If login ever fails for these users, register ONE user through your
-- own application, then copy the hash it stored:
--     SELECT password FROM users WHERE email = 'your@email.com';
-- and replace every hash in this file with that value (Find & Replace).
--
-- Admin logins :  admin@testforge.com  /  sneha.admin@testforge.com
-- Student login:  rahul.sharma@student.com  (and 7 more students)
--
-- Row counts: users 10, topics 10, exams 10, questions 30,
--             exam_questions 41, results 10, student_answers 50,
--             student_weaknesses 10, email_logs 10
--
-- Dates are RELATIVE to the moment you run this file, so exams
-- are always realistically in the past or upcoming. Exam 8 is
-- scheduled 24 hours ahead so the reminder feature can be shown.
-- ============================================================

BEGIN;

-- ---------- clear old data (safe to re-run this file) ----------
DELETE FROM email_logs;
DELETE FROM student_weaknesses;
DELETE FROM student_answers;
DELETE FROM results;
DELETE FROM exam_questions;
DELETE FROM questions;
DELETE FROM exams;
DELETE FROM topics;
DELETE FROM users;

-- ---------- 1. USERS (10) ----------
INSERT INTO users (user_id, name, email, password, role, created_at) VALUES
  (1, 'Admin User', 'admin@testforge.com', '$2b$10$6joF3zDWqYiyMhqVCWYxqefVbHHYTj35H7Ub71sgR51kUG5xqDYrK', 'ADMIN', CURRENT_TIMESTAMP - INTERVAL '60 days'),
  (2, 'Sneha Kulkarni', 'sneha.admin@testforge.com', '$2b$10$6joF3zDWqYiyMhqVCWYxqefVbHHYTj35H7Ub71sgR51kUG5xqDYrK', 'ADMIN', CURRENT_TIMESTAMP - INTERVAL '57 days'),
  (3, 'Rahul Sharma', 'rahul.sharma@student.com', '$2b$10$6joF3zDWqYiyMhqVCWYxqefVbHHYTj35H7Ub71sgR51kUG5xqDYrK', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL '54 days'),
  (4, 'Priya Patel', 'priya.patel@student.com', '$2b$10$6joF3zDWqYiyMhqVCWYxqefVbHHYTj35H7Ub71sgR51kUG5xqDYrK', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL '51 days'),
  (5, 'Amit Desai', 'amit.desai@student.com', '$2b$10$6joF3zDWqYiyMhqVCWYxqefVbHHYTj35H7Ub71sgR51kUG5xqDYrK', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL '48 days'),
  (6, 'Neha Joshi', 'neha.joshi@student.com', '$2b$10$6joF3zDWqYiyMhqVCWYxqefVbHHYTj35H7Ub71sgR51kUG5xqDYrK', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL '45 days'),
  (7, 'Vikram Singh', 'vikram.singh@student.com', '$2b$10$6joF3zDWqYiyMhqVCWYxqefVbHHYTj35H7Ub71sgR51kUG5xqDYrK', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL '42 days'),
  (8, 'Anjali Rao', 'anjali.rao@student.com', '$2b$10$6joF3zDWqYiyMhqVCWYxqefVbHHYTj35H7Ub71sgR51kUG5xqDYrK', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL '39 days'),
  (9, 'Karan Mehta', 'karan.mehta@student.com', '$2b$10$6joF3zDWqYiyMhqVCWYxqefVbHHYTj35H7Ub71sgR51kUG5xqDYrK', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL '36 days'),
  (10, 'Pooja Nair', 'pooja.nair@student.com', '$2b$10$6joF3zDWqYiyMhqVCWYxqefVbHHYTj35H7Ub71sgR51kUG5xqDYrK', 'STUDENT', CURRENT_TIMESTAMP - INTERVAL '33 days');

-- ---------- 2. TOPICS (10) ----------
INSERT INTO topics (topic_id, topic_name, created_at) VALUES
  (1, 'Java Basics', CURRENT_TIMESTAMP - INTERVAL '50 days'),
  (2, 'OOP Concepts', CURRENT_TIMESTAMP - INTERVAL '48 days'),
  (3, 'Collections', CURRENT_TIMESTAMP - INTERVAL '46 days'),
  (4, 'Exception Handling', CURRENT_TIMESTAMP - INTERVAL '44 days'),
  (5, 'SQL Basics', CURRENT_TIMESTAMP - INTERVAL '42 days'),
  (6, 'Joins and Subqueries', CURRENT_TIMESTAMP - INTERVAL '40 days'),
  (7, 'Spring Core', CURRENT_TIMESTAMP - INTERVAL '38 days'),
  (8, 'Spring Boot', CURRENT_TIMESTAMP - INTERVAL '36 days'),
  (9, 'Hibernate and JPA', CURRENT_TIMESTAMP - INTERVAL '34 days'),
  (10, 'Web Technologies', CURRENT_TIMESTAMP - INTERVAL '32 days');

-- ---------- 3. EXAMS (10) ----------
-- Exams 9 and 10 deliberately have NO questions mapped, so they
-- stay hidden from students - this demonstrates the publishing rule.
INSERT INTO exams (exam_id, title, duration_minutes, passing_marks, scheduled_at, created_by, created_at) VALUES
  (1, 'Java Fundamentals Test', 30, 3, CURRENT_TIMESTAMP - INTERVAL '20 days', 1, CURRENT_TIMESTAMP - INTERVAL '38 days'),
  (2, 'OOP and Collections Test', 30, 3, CURRENT_TIMESTAMP - INTERVAL '15 days', 1, CURRENT_TIMESTAMP - INTERVAL '36 days'),
  (3, 'Core Java Assessment', 25, 3, CURRENT_TIMESTAMP - INTERVAL '12 days', 2, CURRENT_TIMESTAMP - INTERVAL '34 days'),
  (4, 'SQL Basics Test', 30, 3, CURRENT_TIMESTAMP - INTERVAL '10 days', 1, CURRENT_TIMESTAMP - INTERVAL '32 days'),
  (5, 'Advanced SQL Test', 25, 3, CURRENT_TIMESTAMP - INTERVAL '8 days', 2, CURRENT_TIMESTAMP - INTERVAL '30 days'),
  (6, 'Spring Framework Test', 30, 3, CURRENT_TIMESTAMP - INTERVAL '5 days', 1, CURRENT_TIMESTAMP - INTERVAL '28 days'),
  (7, 'Spring Boot and JPA Test', 30, 3, CURRENT_TIMESTAMP - INTERVAL '3 days', 2, CURRENT_TIMESTAMP - INTERVAL '26 days'),
  (8, 'Full Stack Mock Test', 45, 4, CURRENT_TIMESTAMP + INTERVAL '24 hours', 1, CURRENT_TIMESTAMP - INTERVAL '24 days'),
  (9, 'Web Technologies Quiz', 20, 2, CURRENT_TIMESTAMP + INTERVAL '5 days', 2, CURRENT_TIMESTAMP - INTERVAL '22 days'),
  (10, 'Final Mock Test', 60, 5, CURRENT_TIMESTAMP + INTERVAL '10 days', 1, CURRENT_TIMESTAMP - INTERVAL '20 days');

-- ---------- 4. QUESTIONS (30) - three per topic ----------
INSERT INTO questions (question_id, topic_id, question_text, option_a, option_b, option_c, option_d, correct_option, created_at) VALUES
  (1, 1, 'Which keyword is used to inherit a class in Java?', 'implements', 'extends', 'inherits', 'super', 'B', CURRENT_TIMESTAMP - INTERVAL '44 days'),
  (2, 1, 'What is the size of an int variable in Java?', '8 bits', '16 bits', '32 bits', '64 bits', 'C', CURRENT_TIMESTAMP - INTERVAL '43 days'),
  (3, 1, 'Which method is the entry point of a Java program?', 'start()', 'run()', 'main()', 'init()', 'C', CURRENT_TIMESTAMP - INTERVAL '42 days'),
  (4, 2, 'Which OOP concept allows several methods with the same name but different parameters?', 'Inheritance', 'Method Overloading', 'Encapsulation', 'Abstraction', 'B', CURRENT_TIMESTAMP - INTERVAL '41 days'),
  (5, 2, 'Which keyword prevents a class from being inherited?', 'static', 'private', 'final', 'abstract', 'C', CURRENT_TIMESTAMP - INTERVAL '40 days'),
  (6, 2, 'Wrapping data and methods into a single unit is called?', 'Polymorphism', 'Inheritance', 'Encapsulation', 'Abstraction', 'C', CURRENT_TIMESTAMP - INTERVAL '39 days'),
  (7, 3, 'Which collection does NOT allow duplicate elements?', 'ArrayList', 'LinkedList', 'HashSet', 'Vector', 'C', CURRENT_TIMESTAMP - INTERVAL '38 days'),
  (8, 3, 'Which interface is implemented by HashMap?', 'List', 'Set', 'Map', 'Queue', 'C', CURRENT_TIMESTAMP - INTERVAL '37 days'),
  (9, 3, 'Which legacy class provides synchronized list operations?', 'ArrayList', 'Vector', 'LinkedList', 'HashSet', 'B', CURRENT_TIMESTAMP - INTERVAL '36 days'),
  (10, 4, 'Which block always executes whether an exception occurs or not?', 'try', 'catch', 'finally', 'throw', 'C', CURRENT_TIMESTAMP - INTERVAL '35 days'),
  (11, 4, 'Which is the parent class of all errors and exceptions in Java?', 'Exception', 'Throwable', 'Error', 'RuntimeException', 'B', CURRENT_TIMESTAMP - INTERVAL '34 days'),
  (12, 4, 'Which keyword is used to manually raise an exception?', 'throws', 'throw', 'raise', 'signal', 'B', CURRENT_TIMESTAMP - INTERVAL '33 days'),
  (13, 5, 'Which SQL keyword removes duplicate rows from a result?', 'UNIQUE', 'DISTINCT', 'REMOVE', 'FILTER', 'B', CURRENT_TIMESTAMP - INTERVAL '32 days'),
  (14, 5, 'Which clause is used to filter rows in a SELECT statement?', 'HAVING', 'GROUP BY', 'WHERE', 'ORDER BY', 'C', CURRENT_TIMESTAMP - INTERVAL '31 days'),
  (15, 5, 'Which SQL command removes a table along with its structure?', 'DELETE', 'TRUNCATE', 'DROP', 'CLEAR', 'C', CURRENT_TIMESTAMP - INTERVAL '30 days'),
  (16, 6, 'Which join returns only the matching rows from both tables?', 'LEFT JOIN', 'RIGHT JOIN', 'INNER JOIN', 'FULL JOIN', 'C', CURRENT_TIMESTAMP - INTERVAL '29 days'),
  (17, 6, 'Which join returns all rows from the left table even without a match?', 'INNER JOIN', 'LEFT JOIN', 'RIGHT JOIN', 'CROSS JOIN', 'B', CURRENT_TIMESTAMP - INTERVAL '28 days'),
  (18, 6, 'A query written inside another query is called?', 'Nested loop', 'Subquery', 'Join', 'View', 'B', CURRENT_TIMESTAMP - INTERVAL '27 days'),
  (19, 7, 'Which annotation is used to mark a business logic class in Spring?', '@Controller', '@Service', '@Entity', '@Bean', 'B', CURRENT_TIMESTAMP - INTERVAL '26 days'),
  (20, 7, 'What does Dependency Injection mainly help to achieve?', 'Tight coupling', 'Loose coupling', 'Faster loops', 'Less memory', 'B', CURRENT_TIMESTAMP - INTERVAL '25 days'),
  (21, 7, 'Which annotation injects a dependency automatically in Spring?', '@Insert', '@Autowired', '@Resourceful', '@Value', 'B', CURRENT_TIMESTAMP - INTERVAL '24 days'),
  (22, 8, 'Which annotation combines configuration, auto configuration and component scan?', '@Configuration', '@EnableAutoConfiguration', '@SpringBootApplication', '@ComponentScan', 'C', CURRENT_TIMESTAMP - INTERVAL '23 days'),
  (23, 8, 'Which file is used to configure settings in a Spring Boot project?', 'web.xml', 'application.properties', 'struts.xml', 'settings.xml', 'B', CURRENT_TIMESTAMP - INTERVAL '22 days'),
  (24, 8, 'Which web server is embedded in Spring Boot by default?', 'Jetty', 'Undertow', 'Tomcat', 'GlassFish', 'C', CURRENT_TIMESTAMP - INTERVAL '21 days'),
  (25, 9, 'Which annotation marks a Java class as a database entity?', '@Table', '@Entity', '@Column', '@Repository', 'B', CURRENT_TIMESTAMP - INTERVAL '20 days'),
  (26, 9, 'Which annotation is used to define the primary key of an entity?', '@Key', '@Id', '@Primary', '@Unique', 'B', CURRENT_TIMESTAMP - INTERVAL '19 days'),
  (27, 9, 'Which fetch type loads related data only when it is accessed?', 'EAGER', 'LAZY', 'AUTO', 'MANUAL', 'B', CURRENT_TIMESTAMP - INTERVAL '18 days'),
  (28, 10, 'Which HTTP method is normally used to create a new resource?', 'GET', 'POST', 'PUT', 'DELETE', 'B', CURRENT_TIMESTAMP - INTERVAL '17 days'),
  (29, 10, 'Which HTTP status code means Not Found?', '200', '301', '404', '500', 'C', CURRENT_TIMESTAMP - INTERVAL '16 days'),
  (30, 10, 'Which HTTP header carries the JWT token in a request?', 'Content-Type', 'Accept', 'Authorization', 'Cookie', 'C', CURRENT_TIMESTAMP - INTERVAL '15 days');

-- ---------- 5. EXAM_QUESTIONS (41) ----------
-- The bridge table. Note question 1 appears in exams 1, 3 and 8:
-- one question can be reused in many exams (many-to-many).
INSERT INTO exam_questions (exam_id, question_id) VALUES
  (1, 1),
  (1, 2),
  (1, 3),
  (1, 4),
  (1, 5),
  (2, 4),
  (2, 5),
  (2, 6),
  (2, 7),
  (2, 8),
  (3, 9),
  (3, 10),
  (3, 11),
  (3, 12),
  (3, 1),
  (4, 13),
  (4, 14),
  (4, 15),
  (4, 16),
  (4, 17),
  (5, 16),
  (5, 17),
  (5, 18),
  (5, 13),
  (5, 14),
  (6, 19),
  (6, 20),
  (6, 21),
  (6, 22),
  (6, 23),
  (7, 22),
  (7, 23),
  (7, 24),
  (7, 25),
  (7, 26),
  (8, 1),
  (8, 7),
  (8, 13),
  (8, 19),
  (8, 28),
  (8, 29);

-- ---------- 6. RESULTS (10) ----------
-- Note results 1 and 10: the same student retook exam 1 and improved
-- from 4 to 5 - a retake creates a NEW attempt, it does not overwrite.
INSERT INTO results (result_id, user_id, exam_id, final_score, exam_date) VALUES
  (1, 3, 1, 4, CURRENT_TIMESTAMP - INTERVAL '19 days'),
  (2, 4, 1, 2, CURRENT_TIMESTAMP - INTERVAL '19 days'),
  (3, 5, 1, 5, CURRENT_TIMESTAMP - INTERVAL '18 days'),
  (4, 3, 4, 3, CURRENT_TIMESTAMP - INTERVAL '9 days'),
  (5, 4, 4, 1, CURRENT_TIMESTAMP - INTERVAL '9 days'),
  (6, 6, 4, 4, CURRENT_TIMESTAMP - INTERVAL '8 days'),
  (7, 3, 6, 2, CURRENT_TIMESTAMP - INTERVAL '4 days'),
  (8, 5, 6, 4, CURRENT_TIMESTAMP - INTERVAL '4 days'),
  (9, 7, 2, 3, CURRENT_TIMESTAMP - INTERVAL '14 days'),
  (10, 3, 1, 5, CURRENT_TIMESTAMP - INTERVAL '2 days');

-- ---------- 7. STUDENT_ANSWERS (50) ----------
-- One row per question of the exam, for every attempt.
-- selected_option NULL means the student skipped that question.
INSERT INTO student_answers (attempt_id, result_id, question_id, selected_option, is_correct) VALUES
  (1, 1, 1, 'B', true),
  (2, 1, 2, 'C', true),
  (3, 1, 3, 'C', true),
  (4, 1, 4, 'B', true),
  (5, 1, 5, 'A', false),
  (6, 2, 1, 'A', false),
  (7, 2, 2, 'C', true),
  (8, 2, 3, 'C', true),
  (9, 2, 4, 'D', false),
  (10, 2, 5, NULL, false),
  (11, 3, 1, 'B', true),
  (12, 3, 2, 'C', true),
  (13, 3, 3, 'C', true),
  (14, 3, 4, 'B', true),
  (15, 3, 5, 'C', true),
  (16, 4, 13, 'B', true),
  (17, 4, 14, 'C', true),
  (18, 4, 15, 'C', true),
  (19, 4, 16, 'A', false),
  (20, 4, 17, 'A', false),
  (21, 5, 13, 'A', false),
  (22, 5, 14, 'C', true),
  (23, 5, 15, 'A', false),
  (24, 5, 16, 'D', false),
  (25, 5, 17, NULL, false),
  (26, 6, 13, 'B', true),
  (27, 6, 14, 'C', true),
  (28, 6, 15, 'C', true),
  (29, 6, 16, 'C', true),
  (30, 6, 17, 'A', false),
  (31, 7, 19, 'A', false),
  (32, 7, 20, 'B', true),
  (33, 7, 21, 'A', false),
  (34, 7, 22, 'C', true),
  (35, 7, 23, 'D', false),
  (36, 8, 19, 'B', true),
  (37, 8, 20, 'B', true),
  (38, 8, 21, 'B', true),
  (39, 8, 22, 'C', true),
  (40, 8, 23, 'A', false),
  (41, 9, 4, 'B', true),
  (42, 9, 5, 'C', true),
  (43, 9, 6, 'A', false),
  (44, 9, 7, 'C', true),
  (45, 9, 8, 'A', false),
  (46, 10, 1, 'B', true),
  (47, 10, 2, 'C', true),
  (48, 10, 3, 'C', true),
  (49, 10, 4, 'B', true),
  (50, 10, 5, 'C', true);

-- ---------- 8. STUDENT_WEAKNESSES (10) ----------
-- Below 50 percent accuracy = NEEDS_REVISION, 80 percent or above = MASTERED.
-- These values match the answers above, so the analytics screens agree
-- with the data.
INSERT INTO student_weaknesses (weakness_id, user_id, topic_id, status, updated_at) VALUES
  (1, 3, 1, 'MASTERED', CURRENT_TIMESTAMP - INTERVAL '4 days'),   -- accuracy 100%
  (2, 3, 5, 'MASTERED', CURRENT_TIMESTAMP - INTERVAL '5 days'),   -- accuracy 100%
  (3, 3, 6, 'NEEDS_REVISION', CURRENT_TIMESTAMP - INTERVAL '6 days'),   -- accuracy 0%
  (4, 3, 7, 'NEEDS_REVISION', CURRENT_TIMESTAMP - INTERVAL '7 days'),   -- accuracy 33%
  (5, 4, 2, 'NEEDS_REVISION', CURRENT_TIMESTAMP - INTERVAL '8 days'),   -- accuracy 0%
  (6, 4, 5, 'NEEDS_REVISION', CURRENT_TIMESTAMP - INTERVAL '9 days'),   -- accuracy 33%
  (7, 4, 6, 'NEEDS_REVISION', CURRENT_TIMESTAMP - INTERVAL '10 days'),   -- accuracy 0%
  (8, 5, 1, 'MASTERED', CURRENT_TIMESTAMP - INTERVAL '11 days'),   -- accuracy 100%
  (9, 5, 2, 'MASTERED', CURRENT_TIMESTAMP - INTERVAL '12 days'),   -- accuracy 100%
  (10, 5, 7, 'MASTERED', CURRENT_TIMESTAMP - INTERVAL '13 days');   -- accuracy 100%

-- ---------- 9. EMAIL_LOGS (10) ----------
-- Two rows are FAILED with sent_at NULL, which is how the system
-- records an email that could not be delivered.
INSERT INTO email_logs (log_id, user_id, exam_id, status, sent_at) VALUES
  (1, 3, 8, 'SENT', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
  (2, 4, 8, 'SENT', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
  (3, 5, 8, 'SENT', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
  (4, 6, 8, 'SENT', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
  (5, 7, 8, 'FAILED', NULL),
  (6, 8, 8, 'SENT', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
  (7, 9, 8, 'SENT', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
  (8, 10, 8, 'FAILED', NULL),
  (9, 3, 7, 'SENT', CURRENT_TIMESTAMP - INTERVAL '4 days'),
  (10, 4, 7, 'SENT', CURRENT_TIMESTAMP - INTERVAL '4 days');

-- ============================================================
-- IMPORTANT: reset the auto-increment counters.
-- We inserted explicit ids, so the sequences still start at 1.
-- Without these lines the application would fail with a
-- duplicate key error the first time it creates a new record.
-- ============================================================
SELECT setval(pg_get_serial_sequence('users', 'user_id'), COALESCE((SELECT MAX(user_id) FROM users), 1));
SELECT setval(pg_get_serial_sequence('topics', 'topic_id'), COALESCE((SELECT MAX(topic_id) FROM topics), 1));
SELECT setval(pg_get_serial_sequence('exams', 'exam_id'), COALESCE((SELECT MAX(exam_id) FROM exams), 1));
SELECT setval(pg_get_serial_sequence('questions', 'question_id'), COALESCE((SELECT MAX(question_id) FROM questions), 1));
SELECT setval(pg_get_serial_sequence('results', 'result_id'), COALESCE((SELECT MAX(result_id) FROM results), 1));
SELECT setval(pg_get_serial_sequence('student_answers', 'attempt_id'), COALESCE((SELECT MAX(attempt_id) FROM student_answers), 1));
SELECT setval(pg_get_serial_sequence('student_weaknesses', 'weakness_id'), COALESCE((SELECT MAX(weakness_id) FROM student_weaknesses), 1));
SELECT setval(pg_get_serial_sequence('email_logs', 'log_id'), COALESCE((SELECT MAX(log_id) FROM email_logs), 1));

COMMIT;

-- ---------- quick check ----------
-- Run these to confirm everything loaded:
--   SELECT 'users' AS t, COUNT(*) FROM users
--   UNION ALL SELECT 'topics', COUNT(*) FROM topics
--   UNION ALL SELECT 'exams', COUNT(*) FROM exams
--   UNION ALL SELECT 'questions', COUNT(*) FROM questions
--   UNION ALL SELECT 'exam_questions', COUNT(*) FROM exam_questions
--   UNION ALL SELECT 'results', COUNT(*) FROM results
--   UNION ALL SELECT 'student_answers', COUNT(*) FROM student_answers
--   UNION ALL SELECT 'student_weaknesses', COUNT(*) FROM student_weaknesses
--   UNION ALL SELECT 'email_logs', COUNT(*) FROM email_logs;