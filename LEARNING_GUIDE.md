# Student Result Management — Hinglish Learning & Interview Guide

## One-minute project introduction

“Maine Java Servlet, JSP, HTML/CSS aur JDBC se Student Result Management portal banaya. Admin accounts aur roles manage karta hai; Teacher aur Admin students aur marks ka CRUD karte hain; Student sirf apne results aur printable report cards dekh sakta hai. Servlet request handle karta hai, validation check karta hai, repository prepared SQL se H2 database update karti hai, aur JSP page render karta hai. Passwords PBKDF2 se hash hote hain; sessions, CSRF tokens, role checks aur HTML escaping bhi use kiye hain.”

## Architecture: WHAT → WHY → HOW

```text
Browser form
   ↓ HTTP request
Tomcat → PortalServlet (route, session, permission, CSRF)
   ↓
Validation → UserRepository / ResultRepository → JDBC → H2 file
   ↓
Servlet request attributes → JSP + JSTL → HTML/CSS response
```

- **WHAT:** Servlet Java class hai jo HTTP request/response handle karti hai. JSP server-side HTML template hai. JDBC Java se SQL database connect karne ka standard API hai.
- **WHY:** UI, request logic, aur SQL alag rakhne se debugging simple hoti hai. Interview mein ise separation of concerns kehte hain.
- **HOW:** `POST /results/create` par `PortalServlet.saveResult()` role/token check ke baad `Validation.result()` call karta hai. `ResultRepository.create()` prepared `INSERT` chalata hai. Successful save par browser `/results` ko redirect hota hai; JSP updated table dikhata hai.
- **CODE:** `results.create(studentId, form.get("subject"), Validation.number(form.get("marks")), ...);`
- **PROJECT CONNECTION:** Servlet → validation → repository → database → JSP.
- **INTERVIEW:** “Servlet aur JSP mein difference?” Servlet processing/control, JSP presentation.
- **COMMON MISTAKE:** Sirf button hide karke authorization samajhna. Direct URL/POST bhi server par block hona chahiye; is project mein hota hai.

## Important files

| File | Purpose and connection |
| --- | --- |
| `pom.xml` | Maven dependencies, Java 21 target, WAR packaging for Tomcat. |
| `run.ps1` | WAR build, verified Tomcat download, deployment, persistent data path, server start. |
| `web/AppContext.java` | Tomcat startup par database schema/seed initialize karta hai; repositories application scope mein rakhta hai. |
| `web/PortalServlet.java` | `doGet`/`doPost`, routes, sessions, role/CSRF checks, form processing, redirects, JSP forwarding. |
| `service/Validation.java` | Server-side checks for required fields, email, marks, year, IDs. |
| `repository/UserRepository.java` | Account SQL, password authentication, email/roll uniqueness handling. |
| `repository/ResultRepository.java` | Marks CRUD, search, duplicate prevention, report queries. |
| `database/Database.java` | H2 JDBC URL, schema, constraints, demo seed. `SRM_DATA_DIR` decides file location. |
| `model/User.java`, `Result.java`, `Role.java` | Data objects; `Result` calculates percentage/grade/status. Bean getters let JSP EL access fields. |
| `security/PasswordUtil.java` | Salted PBKDF2 password hashing and comparison; plaintext password database mein nahi rakhta. |
| `src/main/webapp/WEB-INF/jsp/` | Login, dashboard, list/form/report/error views. `WEB-INF` prevents direct browser access to JSP files. |
| `src/main/webapp/style.css` | Warm Academia design, responsive breakpoints, printable report styles. |
| `scripts/verify-servlet.ps1` | Running Tomcat par actual HTTP flow tests. Disposable database par hi run karo. |

## From scratch rebuild: practical sequence

### 1. Project skeleton

- **WHAT:** Maven WAR project banao; `src/main/java` mein Java, `src/main/webapp` mein JSP/CSS.
- **WHY:** WAR Tomcat deploy karta hai; ordinary JAR ab correct output nahi hai.
- **HOW/CODE:** `pom.xml` mein `<packaging>war</packaging>` aur `jakarta.servlet-api` provided scope rakho.
- **PROJECT CONNECTION:** `mvn package` → `target/student-result-management-1.0.0.war`.
- **INTERVIEW:** “Provided scope kyun?” Tomcat Servlet API already deta hai, WAR mein duplicate nahi chahiye.
- **COMMON MISTAKE:** `java -jar` se WAR run karna.

### 2. Database and models

- **WHAT:** `users` aur `results` tables; `student_id` foreign key; unique email/roll/result combination.
- **WHY:** Data restart ke baad bhi rahe, aur duplicates/inconsistent rows DB level par rukhein.
- **HOW/CODE:** `PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id = ?"); statement.setLong(1, id);`
- **PROJECT CONNECTION:** `Database.initialize()` schema banata hai; repositories queries chalati hain.
- **INTERVIEW:** “Prepared statement?” SQL aur input separate rehte hain, SQL injection ka risk kam hota hai.
- **COMMON MISTAKE:** SQL string mein user input concatenate karna.

### 3. Servlet routes and sessions

- **WHAT:** `PortalServlet` URL ko Java method se map karta hai; `HttpSession` signed-in user ID rakhta hai.
- **WHY:** Har browser request ko identity aur permission check chahiye.
- **HOW/CODE:** `request.getSession(true).setAttribute("userId", user.id());` and `users.findById(id)` on later requests.
- **PROJECT CONNECTION:** Login ke baad `/dashboard`; Student ko `/users` direct URL par 403.
- **INTERVIEW:** “Session cookie mein kya hota hai?” Browser mein opaque session ID; role/marks cookie mein nahi. Server session ID se user lookup hota hai.
- **COMMON MISTAKE:** Browser-provided role ko trust karna.

### 4. Validation and CRUD

- **WHAT:** Save se pehle fields, role, marks range, year, ownership and CSRF check.
- **WHY:** HTML `required` bypass ho sakta hai; DB ko invalid data nahi milna chahiye.
- **HOW/CODE:** `Validation.result(form);` then `results.create(...)`; errors par same JSP form preserved values ke saath status 400.
- **PROJECT CONNECTION:** Student and result create/update/delete routes.
- **INTERVIEW:** “Duplicate ko kaise handle karte ho?” Repository friendly pre-check karta hai, DB unique index race condition mein final guard hai.
- **COMMON MISTAKE:** Sirf frontend validation par rely karna.

### 5. JSP pages and CSS

- **WHAT:** Servlet `request.setAttribute("results", data)` karke JSP forward karta hai; JSP `c:forEach` se rows dikhata hai.
- **WHY:** Java mein HTML strings banana mushkil; JSP presentation readable banata hai.
- **HOW/CODE:** `<c:out value="${result.subject}"/>` user text escape karta hai; CSS mobile layout and print styles deta hai.
- **PROJECT CONNECTION:** `results.jsp`, `report.jsp`, shared `header.jspf`/`footer.jspf`.
- **INTERVIEW:** “XSS kaise roka?” User text `c:out` se HTML-escaped hai; server validation bhi hai.
- **COMMON MISTAKE:** `${userText}` ko bina escaping raw HTML mein output karna.

### 6. Run and verify

- **WHAT:** `mvn package`, then `.\run.ps1`; browser at `http://localhost:8080/login`.
- **WHY:** Compilation success se JSP runtime success prove nahi hota. Tomcat mein deploy karke actual forms test karo.
- **HOW/CODE:** `scripts/verify-servlet.ps1` disposable DB par sign-in, CRUD, roles, CSRF, privacy test karta hai.
- **PROJECT CONNECTION:** Interview se pehle same demo flow repeat karo.
- **INTERVIEW:** “Testing strategy?” 11 Java model/validation/repository tests plus deployed HTTP flow script.
- **COMMON MISTAKE:** Test script live demo database par chalana; woh test rows create/delete karta hai.

## Concepts: simple meaning → analogy → project example → likely question

| Concept | Simple meaning / analogy | Project example | Interview question |
| --- | --- | --- | --- |
| HTTP GET/POST | GET padhna, POST badalna; library mein dekhna vs form submit | GET `/results`, POST `/results/create` | GET se delete kyun nahi? |
| Servlet | Request ka Java controller; receptionist | `PortalServlet.doPost()` | `doGet` aur `doPost`? |
| JSP/EL/JSTL | HTML template; form par filled data | `c:forEach` rows, `c:out` escaping | JSP direct access kyun blocked? |
| Session | Server-side login memory; visitor pass | `userId` in `HttpSession` | Cookie vs session? |
| CSRF | Forged form request rokna | Hidden `csrf` token checked on POST | CSRF token kyun? |
| JDBC | Java–SQL bridge; translator | `PreparedStatement` | Connection close kaise? |
| Foreign key | Related row validity; roll-call link | `results.student_id → users.id` | Student delete par result? |
| Hash + salt | Password one-way transform + randomness | `PasswordUtil` PBKDF2 | Plaintext kyun nahi? |
| WAR/Tomcat | Web app package + web server | `mvn package`, Tomcat deploy | WAR vs JAR? |

## Interview-ready questions and answers

1. **Project kya solve karta hai?** Academic records ko ek role-based portal mein save, update, search, aur report ke form mein present karta hai. Follow-up: data kahan persist hota hai? H2 file under `data`.
2. **Stack kyun choose ki?** User requirement Java/HTML/CSS/JDBC aur ab Servlet/JSP thi. Tomcat + server-rendered JSP is size ke CRUD app ke liye simple, defensible stack hai. Follow-up: React kyun nahi? Extra API/frontend state ki zaroorat nahi thi.
3. **Data flow?** Browser form → Servlet authentication/authorization/validation → repository JDBC SQL → H2 → JSP response. Follow-up: failed validation? 400 + form with error and original values.
4. **Role security?** Har protected request par database se current user load hota hai; staff/admin route checks server-side hain; report par student ID ownership check hai. Follow-up: direct URL? Same check applies.
5. **Password security?** Random salt + PBKDF2 hash store hota hai; session password hash version check karta hai, so password change old sessions revoke karta hai. Follow-up: public deployment? HTTPS, secure cookies, rate limiting, no demo credentials.
6. **Duplicate requests?** Unique DB constraints plus friendly repository validation. Follow-up: do users same time submit? DB index final guard; friendly message returned.
7. **Report calculation?** Selected result se same student/exam/year subjects group karte hain; total marks, weighted percentage, all-subject pass status calculate hota hai. Follow-up: average of percentages? Unequal max marks mein wrong, so summed marks / summed maximum.
8. **Hardest part?** Servlet/JSP migration mein JDBC driver loading under Tomcat classloader aur JSP taglib include order fix kiya; deployed app run karke catch kiya. Follow-up: Java unit tests ne kyun nahi pakda? Container/JSP runtime alag environment hai.
9. **Scale kaise?** H2 se managed relational DB, connection pool, migrations, paginated results, login throttling, monitoring. Ye future improvements hain, current implemented features nahi.
10. **What not to claim?** Email ownership verification, password reset, public-production security, cloud deployment, full automation CI, or MySQL use claim mat karo. Actual DB H2 JDBC hai; login email/password se hota hai.

## Safe demo flow

1. Teacher sign in → dashboard → student create/update → result create → search → report/print → result delete → student delete.
2. Student sign in → own results → explain direct other-student report returns 403.
3. Admin sign in → users page → create/edit/delete a temporary teacher.
4. Explain schema, prepared statements, session, CSRF, and one trade-off.

## 12-day preparation plan

| Day | Focus |
| --- | --- |
| 1 | Run app, navigate all roles, draw request-to-database flow. |
| 2 | Read `PortalServlet` GET/POST, session and permission checks. |
| 3 | Read JSP pages, models, CSS, then explain one page without notes. |
| 4 | Rebuild login and dashboard in a fresh practice branch/folder. |
| 5 | Rebuild student CRUD and server validation. |
| 6 | Rebuild result CRUD/report calculations. |
| 7 | Practice Servlet, JSP, HTTP, session, CSRF and XSS answers. |
| 8 | Practice JDBC, SQL schema, constraints, prepared statements, PBKDF2. |
| 9 | Answer 10 project questions aloud, 60–90 seconds each. |
| 10 | Practice follow-ups, edge cases and honest limitations. |
| 11 | Full mock interview + live demo from a clean start. |
| 12 | Final revision; run `mvn package`, start app, verify all demo accounts. |
