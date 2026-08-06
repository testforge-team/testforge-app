-- ============================================================
-- TestForge : An Interactive Exam Portal Where Skills Are Forged
-- PostgreSQL DDL Script — 9 Tables
-- Run order respects foreign key dependencies.
-- ============================================================

-- 1. USERS ----------------------------------------------------
CREATE TABLE users (
    user_id      SERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,          -- BCrypt hash, never plain text
    role         VARCHAR(10)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN ('STUDENT', 'ADMIN'))
);

-- 2. TOPICS ---------------------------------------------------
CREATE TABLE topics (
    topic_id     SERIAL PRIMARY KEY,
    topic_name   VARCHAR(100) NOT NULL UNIQUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. EXAMS ----------------------------------------------------
CREATE TABLE exams (
    exam_id          SERIAL PRIMARY KEY,
    title            VARCHAR(150) NOT NULL,
    duration_minutes INT          NOT NULL,
    passing_marks    INT          NOT NULL,      -- min score to PASS (1 mark/question)
    scheduled_at     TIMESTAMP    NOT NULL,      -- when the exam OPENS; drives reminders
    active_hours     INT          NOT NULL DEFAULT 24,  -- stays open this many hours
    created_by       INT          NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exams_creator   FOREIGN KEY (created_by) REFERENCES users(user_id),
    CONSTRAINT chk_exam_duration  CHECK (duration_minutes > 0),
    CONSTRAINT chk_passing_marks  CHECK (passing_marks >= 0),
    CONSTRAINT chk_active_hours   CHECK (active_hours > 0)
);
-- The exam window is:  opens at scheduled_at, closes at scheduled_at + active_hours.
-- Status (NOT_STARTED / ACTIVE / EXPIRED) is CALCULATED from these two columns,
-- never stored, so it can never become stale.

-- 4. QUESTIONS ------------------------------------------------
CREATE TABLE questions (
    question_id    SERIAL PRIMARY KEY,
    topic_id       INT          NOT NULL,
    question_text  TEXT         NOT NULL,
    option_a       VARCHAR(255) NOT NULL,
    option_b       VARCHAR(255) NOT NULL,
    option_c       VARCHAR(255) NOT NULL,
    option_d       VARCHAR(255) NOT NULL,
    correct_option CHAR(1)      NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_topic  FOREIGN KEY (topic_id) REFERENCES topics(topic_id),
    CONSTRAINT chk_correct_option  CHECK (correct_option IN ('A', 'B', 'C', 'D'))
);

-- 5. EXAM_QUESTIONS (junction: M:N between exams & questions) -
CREATE TABLE exam_questions (
    exam_id      INT NOT NULL,
    question_id  INT NOT NULL,
    CONSTRAINT pk_exam_questions   PRIMARY KEY (exam_id, question_id),
    CONSTRAINT fk_eq_exam          FOREIGN KEY (exam_id)     REFERENCES exams(exam_id)         ON DELETE CASCADE,
    CONSTRAINT fk_eq_question      FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- 6. RESULTS --------------------------------------------------
CREATE TABLE results (
    result_id    SERIAL PRIMARY KEY,
    user_id      INT       NOT NULL,
    exam_id      INT       NOT NULL,
    final_score  INT       NOT NULL DEFAULT 0,   -- count of correct answers
    exam_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- submission time
    started_at   TIMESTAMP,                      -- when the student pressed Start
    end_time     TIMESTAMP,                      -- server-decided deadline for THIS attempt
    status       VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    CONSTRAINT fk_results_user  FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_results_exam  FOREIGN KEY (exam_id) REFERENCES exams(exam_id),
    CONSTRAINT chk_final_score  CHECK (final_score >= 0),
    CONSTRAINT chk_result_status CHECK (status IN
        ('IN_PROGRESS', 'SUBMITTED', 'AUTO_SUBMITTED', 'EXPIRED'))
);
-- A row is created the MOMENT the student presses Start (status IN_PROGRESS),
-- not at submission. end_time is stored on the server, so the countdown shown in
-- the browser is derived from it -- refreshing the page cannot reset or extend it.
--
-- A student may have only ONE unfinished attempt per exam. This partial unique
-- index enforces that, while still allowing unlimited finished attempts (retakes).
CREATE UNIQUE INDEX idx_one_active_attempt
    ON results (user_id, exam_id)
    WHERE status = 'IN_PROGRESS';

-- Used by the background job that closes abandoned attempts.
CREATE INDEX idx_results_status_endtime ON results (status, end_time);

-- 7. STUDENT_ANSWERS ------------------------------------------
CREATE TABLE student_answers (
    attempt_id      SERIAL PRIMARY KEY,
    result_id       INT     NOT NULL,
    question_id     INT     NOT NULL,
    selected_option CHAR(1),                     -- NULL = question skipped
    is_correct      BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_sa_result    FOREIGN KEY (result_id)   REFERENCES results(result_id) ON DELETE CASCADE,
    CONSTRAINT fk_sa_question  FOREIGN KEY (question_id) REFERENCES questions(question_id),
    CONSTRAINT chk_selected    CHECK (selected_option IN ('A', 'B', 'C', 'D') OR selected_option IS NULL)
);
-- Answers are now saved DURING the exam (auto-save on every click), so the same
-- question must never get two rows in one attempt. Changing an answer UPDATES
-- this row instead of inserting another.
CREATE UNIQUE INDEX idx_answer_unique ON student_answers (result_id, question_id);

-- 8. STUDENT_WEAKNESSES ---------------------------------------
CREATE TABLE student_weaknesses (
    weakness_id  SERIAL PRIMARY KEY,
    user_id      INT         NOT NULL,
    topic_id     INT         NOT NULL,
    status       VARCHAR(20) NOT NULL,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sw_user     FOREIGN KEY (user_id)  REFERENCES users(user_id),
    CONSTRAINT fk_sw_topic    FOREIGN KEY (topic_id) REFERENCES topics(topic_id),
    CONSTRAINT uq_user_topic  UNIQUE (user_id, topic_id),
    CONSTRAINT chk_sw_status  CHECK (status IN ('NEEDS_REVISION', 'MASTERED'))
);

-- 9. EMAIL_LOGS -----------------------------------------------
CREATE TABLE email_logs (
    log_id    SERIAL PRIMARY KEY,
    user_id   INT         NOT NULL,
    exam_id   INT         NOT NULL,
    status    VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    sent_at   TIMESTAMP,                          -- NULL until successfully sent
    CONSTRAINT fk_el_user    FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_el_exam    FOREIGN KEY (exam_id) REFERENCES exams(exam_id),
    CONSTRAINT chk_el_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

-- ============================================================
-- Helpful indexes (FKs used in dashboard / analytics queries)
-- ============================================================
CREATE INDEX idx_questions_topic      ON questions(topic_id);
CREATE INDEX idx_results_user         ON results(user_id);
CREATE INDEX idx_results_exam         ON results(exam_id);
CREATE INDEX idx_sa_result            ON student_answers(result_id);
CREATE INDEX idx_sa_question          ON student_answers(question_id);
CREATE INDEX idx_sw_user              ON student_weaknesses(user_id);
CREATE INDEX idx_el_exam              ON email_logs(exam_id);
CREATE INDEX idx_exams_scheduled      ON exams(scheduled_at);   -- reminder job scans this

-- ============================================================
-- Sample seed data (optional — delete before production)
-- ============================================================
INSERT INTO users (name, email, password, role) VALUES
('Faculty Admin', 'admin@testforge.app', '$2a$10$examplehashreplacethis', 'ADMIN');

INSERT INTO topics (topic_name) VALUES
('Java - OOP Concepts'), ('Collections'), ('Exception Handling'),
('SQL Joins'), ('Hibernate'), ('React Components');
