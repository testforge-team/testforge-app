# TestForge

An online exam portal built as our PGCP-AC project at C-DAC.

Admins create topics, add questions to a question bank and set up exams.
Students log in, attempt an exam against a timer and get their result
immediately. The system checks the answers itself, works out the percentage
and pass/fail, and keeps every answer so it can also show which topics a
student is weak in. Admins get a report of the topics the whole batch finds
difficult, and reminder emails go out before each exam.

## What it does

- Registration and login with JWT tokens, passwords stored as BCrypt hashes
- Two roles: STUDENT and ADMIN, with different screens and permissions
- Topic and question bank management (four options, one correct answer)
- Exam creation with duration, passing marks and schedule
- Questions are mapped to exams, so the same question can be reused
- Timer based exam attempt that auto-submits when the time is over
- Automatic evaluation, one mark per question, instant result
- Answer sheet review with a topic-wise breakdown
- Weak topic detection per student, difficult topic report for the batch
- Reminder emails, sent manually or automatically 24 hours before an exam

## Built with

Backend: Java 17, Spring Boot 3.3, Spring Security (JWT), Hibernate/JPA, Maven
Frontend: React, Vite, Axios, React Router, Bootstrap
Database: PostgreSQL
Tools: Eclipse, VS Code, Postman, pgAdmin, Git

## Folder structure

    TestForge/
        backend/        Spring Boot project (open this one in Eclipse)
        frontend/       React project (open this one in VS Code)
        database/
            TestForge_schema.sql        creates the 9 tables
            TestForge_sample_data.sql   optional demo data

## Setting it up

You need JDK 17, PostgreSQL, Node.js 18 or above, Eclipse and VS Code.

### 1. Database

Open pgAdmin and create a database called `testforge`. Open the Query Tool
on it and run `database/TestForge_schema.sql`. You should end up with 9
tables under Schemas > public > Tables.

If you want some data to work with, run `database/TestForge_sample_data.sql`
as well. It adds 10 users, 10 topics, 30 questions, 10 exams and some past
attempts. Every user in that file has the password `password123`.

### 2. Backend

Lombok has to be installed into Eclipse itself, otherwise the editor shows
hundreds of errors that are not really there. Download `lombok.jar` from
projectlombok.org, run it with `java -jar lombok.jar`, point it at your
Eclipse folder, install, and restart Eclipse.

Then import the project: File > Import > Maven > Existing Maven Projects,
and select the `backend` folder.

Create `backend/src/main/resources/application.properties` (it is not in
Git because it holds passwords) using this as a starting point:

    spring.datasource.url=jdbc:postgresql://localhost:5432/testforge
    spring.datasource.username=postgres
    spring.datasource.password=YOUR_DB_PASSWORD

    spring.jpa.hibernate.ddl-auto=none
    spring.jpa.show-sql=true

    app.jwt.secret=any-long-random-string-at-least-32-characters
    app.jwt.expiration-ms=86400000

    server.port=8080

    # only needed for the reminder emails
    spring.mail.host=smtp.gmail.com
    spring.mail.port=587
    spring.mail.username=your.email@gmail.com
    spring.mail.password=16_CHARACTER_APP_PASSWORD
    spring.mail.properties.mail.smtp.auth=true
    spring.mail.properties.mail.smtp.starttls.enable=true

For the mail password use a Gmail App Password, not your normal one.
Turn on 2-Step Verification first, then create an App Password under
Google Account > Security. If you skip the mail settings the app still
runs, it just logs the emails as FAILED.

Now run it: right-click the project > Run As > Spring Boot App. The console
should end with `Started TestForgeApplication` and the API is then on
http://localhost:8080.

### 3. Frontend

    cd frontend
    npm install
    npm run dev

Open http://localhost:5173. Start the backend before this, or every page
will load but nothing will work.

### 4. Make yourself an admin

New accounts are always students. Register once through the app, then run
this in pgAdmin:

    UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';

Log out and log in again to pick up the new role.

## Logins for the sample data

    Admin      admin@testforge.com          password123
    Student    rahul.sharma@student.com     password123

## API endpoints

Everything except the two auth endpoints needs the header
`Authorization: Bearer <token>`.

    POST   /api/auth/register
    POST   /api/auth/login

    GET    /api/users                        admin
    GET    /api/users/{id}                   admin

    GET    /api/topics
    POST   /api/topics                       admin
    PUT    /api/topics/{id}                  admin
    DELETE /api/topics/{id}                  admin

    GET    /api/questions                    admin
    POST   /api/questions                    admin
    PUT    /api/questions/{id}               admin
    DELETE /api/questions/{id}               admin

    GET    /api/exams                        exams a student can take
    GET    /api/exams/all                    admin, includes drafts
    GET    /api/exams/{id}/attempt           questions without the answers
    POST   /api/exams                        admin
    PUT    /api/exams/{id}                   admin
    DELETE /api/exams/{id}                   admin
    POST   /api/exams/{id}/questions         admin, map questions

    POST   /api/exams/{id}/submit            submit and get graded
    GET    /api/results/{studentId}
    GET    /api/results/detail/{resultId}

    GET    /api/analytics/student/{id}
    GET    /api/analytics/difficult-topics   admin
    GET    /api/dashboard/student/{id}
    GET    /api/dashboard/admin              admin

    POST   /api/reminders/send/{examId}      admin
    GET    /api/reminders/logs               admin

There is also a scheduled job in the backend that sends reminders on its
own, roughly 24 hours before each exam.

## Database tables

    users                accounts, role and hashed password
    topics               subject categories
    questions            question bank, four options and the answer
    exams                title, duration, passing marks, schedule
    exam_questions       which questions belong to which exam
    results              one row per exam attempt
    student_answers      one row per question of an attempt
    student_weaknesses   topic status per student
    email_logs           record of every reminder email

An exam only becomes visible to students once at least one question has been
mapped to it, so a newly created exam stays a draft until you add questions.

## If something does not work

- Red CORS error in the browser console: the backend is not running.
- Everything returns 401: the token expired, log in again.
- 403 on an admin page: you are logged in as a student.
- 500 error: read the stack trace in the Eclipse console, not the browser.
- Eclipse shows errors on every entity: Lombok is not installed in Eclipse.
- Timer or dashboard shows undefined: log out and log in again so the
  session is stored freshly.

## Team

- Sayali Narvekar
- Swati Dixit
- Sukruti Dhole
- Vinay Dharurkar

Project guide: Mr. Abhilash Bande
ACTS C-DAC, Pune 
