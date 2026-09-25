# Student Result Management System

A beginner-friendly Java Servlet/JSP/JDBC project for managing student records and report cards. The Warm Academia interface is server-rendered and responsive.

## Features

- Admin manages accounts and roles; Admin and Teacher manage students and results; Student sees only their own results.
- Full create, read, update, delete flows for student details and marks.
- Searchable result archive, automatic grade/pass calculation, grouped printable report card.
- H2 file database through JDBC prepared statements; passwords hashed with PBKDF2.
- Server-side validation, container-managed sessions, CSRF tokens, output escaping, and role/ownership checks.

## Run on this Windows PC

Requires JDK 21+ and Maven 3.9+. From PowerShell in this folder:

```powershell
.\run.ps1
```

On first run, the script downloads and SHA-512-verifies Apache Tomcat 10.1.60 into `target/local-tomcat`. It builds the WAR, deploys it as the root web app, and runs Tomcat on `http://localhost:8080/login`. Keep the PowerShell window open; press Ctrl+C to stop. It keeps database files in the project's `data` folder across restarts. If PowerShell blocks scripts, run `powershell -ExecutionPolicy Bypass -File .\run.ps1` for this one invocation.

Do **not** use the old `java -jar target/student-result-management-1.0.0.jar` command; this project now builds a WAR for Tomcat. If port 8080 is occupied, stop the old server first.

## Run with an existing Tomcat 10.1 installation

```powershell
mvn package
$env:SRM_DATA_DIR = (Join-Path (Get-Location) 'data')
Copy-Item 'target\student-result-management-1.0.0.war' 'C:\path\to\tomcat\webapps\ROOT.war'
& 'C:\path\to\tomcat\bin\catalina.bat' run
```

Remove Tomcat's default `webapps/ROOT` folder before deploying as `ROOT.war`, or rename the WAR and use its context path. Set `SRM_DATA_DIR` so Tomcat finds the same project database; otherwise the default `data` path is relative to Tomcat's working directory.

## Demo accounts

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@school.com` | `admin123` |
| Teacher | `teacher@school.com` | `teacher123` |
| Student | `student@school.com` | `student123` |

These seeded accounts exist only when a database is first created. This is a **local interview demo**, not a production-ready public service. Email is the sign-in identifier; there is no email ownership verification or password reset. Before public deployment, remove demo credentials and add HTTPS, secure cookie settings, login rate limiting, backups, and operational monitoring.

## Structure

```text
src/main/java/com/studentresults/
  database/Database.java          schema, H2 connection and seed data
  model/                       User, Result, Role
  repository/                  JDBC queries and CRUD
  security/PasswordUtil.java
  service/Validation.java
  web/AppContext.java           database initialization on Tomcat startup
  web/PortalServlet.java        HTTP routes, sessions, roles, forms, JSP forwarding
src/main/webapp/
  index.jsp                    root redirect
  style.css                    Warm Academia design
  WEB-INF/jsp/                 JSP pages and shared header/footer
scripts/verify-servlet.ps1     functional checks against a running test instance
```

Browser → Servlet → validation/repository → H2 → Servlet forwards to JSP → HTML response. The browser never calls SQL directly. JSP uses JSTL/EL to render data and `c:out` to escape user-entered text.

## Checks

`mvn package` runs the Java unit/repository tests and builds the WAR. `scripts/verify-servlet.ps1` exercises login, roles, CSRF, CRUD, validation, search, reports and logout against a running Tomcat instance. Run it only against a disposable database, because it creates and deletes test records.

See [LEARNING_GUIDE.md](LEARNING_GUIDE.md) for the Hinglish code walkthrough and interview preparation.
