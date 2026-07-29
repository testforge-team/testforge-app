# TestForge

An interactive exam portal. Admins create topics, questions and exams;
students take exams, get graded automatically, and can see their results and weak
areas. Built as a college group project by 4 people.

Java 17 · Spring Boot 3.3 · Spring Security (JWT) · PostgreSQL · Maven

## What it does

- Register / login with JWT tokens (roles: STUDENT and ADMIN)
- Admin manages topics, a question bank, and exams
- Students attempt exams and answers are graded automatically
- Results are stored with a topic-wise breakdown so students see where they're weak
- Analytics + a simple dashboard for admin and students
- Automatic email reminders before an exam (scheduled job)

## Tech stack

- Java 17, Spring Boot 3.3
- Spring Security with JWT for auth
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven (build)
- Lombok (install it in your IDE or you'll see fake errors)

## Getting started

You need JDK 17+, PostgreSQL, Maven, and Lombok set up in your IDE.

1. Create a database named `testforge` in pgAdmin.
2. Open the query tool and run `db/TestForge_schema.sql`. This creates all the tables.
3. Copy the example config and fill in your own values:

   ```
   copy src\main\resources\application.properties.example src\main\resources\application.properties
   ```

   Then edit `application.properties`:
   - `spring.datasource.password` = your postgres password
   - `app.jwt.secret` = a long random string (run `openssl rand -base64 32` to make one)

4. Run the app. In Eclipse: right-click the project > Run As > Spring Boot App.
   Or from a terminal: `mvn spring-boot:run`
5. It starts on `http://localhost:8080`.

Note: `application.properties` is gitignored on purpose because it has your password
and secret. Only the `.example` file is in the repo. Everyone makes their own local copy.

## First admin user

The API only creates STUDENT accounts. To get an admin, register a normal user,
then flip the role in the database:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

## API overview

Auth is a Bearer token. Log in, copy the `token` from the response, and send it as
a header on protected calls: `Authorization: Bearer <token>`.

Public:
- `POST /api/auth/register` - create a student account
- `POST /api/auth/login` - get a token

Topics (read = any logged-in user, write = admin):
- `GET /api/topics`
- `POST /api/topics`, `PUT /api/topics/{id}`, `DELETE /api/topics/{id}`

Questions (admin only):
- `GET /api/questions` (add `?topicId=3` to filter by topic)
- `POST /api/questions`, `PUT /api/questions/{id}`, `DELETE /api/questions/{id}`

Exams:
- `GET /api/exams` - list exams
- `GET /api/exams/{id}/attempt` - questions for taking the exam (no answers shown)
- admin: `GET /api/exams/all`, `POST /api/exams`, `PUT`, `DELETE`, `POST /api/exams/{id}/questions`

Results:
- `POST /api/exams/{id}/submit` - submit answers, get graded
- `GET /api/results/{studentId}` - a student's past results
- `GET /api/results/detail/{resultId}` - full breakdown of one result

Users (admin only):
- `GET /api/users`, `GET /api/users/{id}`

Analytics & dashboard (admin only unless noted):
- `GET /api/analytics/student/{id}`, `GET /api/analytics/difficult-topics`
- `GET /api/dashboard/student/{id}`, `GET /api/dashboard/admin`

Reminders (admin only):
- `POST /api/reminders/send/{examId}`, `GET /api/reminders/logs`

## Who built what

| Person | Area |
|--------|------|
| A | Project setup, config, security (JWT), auth, users, shared exception handling |
| B | Topics, question bank, exams |
| C | Exam attempts and grading (results, student answers) |
| D | Analytics, reports, email notifications, dashboards |

## Branches

- `dev` - where everyone's work is merged and integrated
- `test` - promoted from dev once things are stable
- `final-production` - the release branch

Each person works on their own feature branch and opens a pull request into `dev`.

## Notes

- If you see lots of red errors right after importing, it's almost always Lombok
  not installed in the IDE. Install it and restart.
- If a call returns 401, you forgot the token. If it returns 403, your token is a
  student but the endpoint needs an admin.
