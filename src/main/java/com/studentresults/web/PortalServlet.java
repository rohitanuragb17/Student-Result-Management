package com.studentresults.web;

import com.studentresults.model.Result;
import com.studentresults.model.Role;
import com.studentresults.model.User;
import com.studentresults.repository.ResultRepository;
import com.studentresults.repository.UserRepository;
import com.studentresults.service.Validation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/login", "/logout", "/dashboard", "/students", "/students/new",
        "/students/edit", "/students/create", "/students/update", "/students/delete",
        "/users", "/users/new", "/users/edit", "/users/create", "/users/update", "/users/delete",
        "/results", "/results/new", "/results/edit", "/results/create", "/results/update",
        "/results/delete", "/my-results", "/report"})
public final class PortalServlet extends HttpServlet {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private final SecureRandom random = new SecureRandom();
    private UserRepository users;
    private ResultRepository results;

    @Override
    public void init() {
        users = (UserRepository) getServletContext().getAttribute("users");
        results = (ResultRepository) getServletContext().getAttribute("results");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Content-Security-Policy", "default-src 'self'; style-src 'self'; script-src 'unsafe-inline'");
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        request.setAttribute("activeSection", path.startsWith("/students") ? "students"
                : path.startsWith("/users") ? "users"
                : path.startsWith("/results") || path.equals("/report") ? "results"
                : path.equals("/my-results") ? "my-results" : "dashboard");
        boolean post = request.getMethod().equals("POST");
        try {
            if (path.equals("/login")) {
                if (post) login(request, response);
                else if (currentUser(request) != null) redirect(request, response, "/dashboard");
                else view(request, response, "login");
                return;
            }
            User user = currentUser(request);
            if (user == null) {
                redirect(request, response, "/login");
                return;
            }
            request.setAttribute("user", user);
            request.setAttribute("csrf", request.getSession(false).getAttribute("csrf"));
            request.setAttribute("firstName", firstName(user.fullName()));
            request.setAttribute("initials", initials(user.fullName()));
            if (post && !request.getSession(false).getAttribute("csrf").equals(request.getParameter("csrf"))) {
                throw new Forbidden("Invalid security token. Refresh the page and try again.");
            }
            String route = (post ? "POST " : "GET ") + path;
            switch (route) {
                case "POST /logout" -> { request.getSession(false).invalidate(); redirect(request, response, "/login"); }
                case "GET /dashboard" -> dashboard(request, response, user);
                case "GET /students" -> { staff(user); listStudents(request, response); }
                case "GET /students/new" -> { staff(user); userForm(request, response, null, true, null); }
                case "GET /students/edit" -> { staff(user); User edit = findUser(request); student(edit); userForm(request, response, edit, true, null); }
                case "POST /students/create" -> { staff(user); saveUser(request, response, null, true); }
                case "POST /students/update" -> { staff(user); User edit = findUserFromForm(request); student(edit); saveUser(request, response, edit, true); }
                case "POST /students/delete" -> { staff(user); User edit = findUserFromForm(request); student(edit); users.delete(edit.id()); success(request, response, "/students", "Student and linked results deleted."); }
                case "GET /users" -> { admin(user); listUsers(request, response); }
                case "GET /users/new" -> { admin(user); userForm(request, response, null, false, null); }
                case "GET /users/edit" -> { admin(user); userForm(request, response, findUser(request), false, null); }
                case "POST /users/create" -> { admin(user); saveUser(request, response, null, false); }
                case "POST /users/update" -> { admin(user); saveUser(request, response, findUserFromForm(request), false); }
                case "POST /users/delete" -> { admin(user); long id = Validation.id(request.getParameter("id")); if (id == user.id()) throw new IllegalArgumentException("You cannot delete your own signed-in account."); users.delete(id); success(request, response, "/users", "User deleted successfully."); }
                case "GET /results" -> { staff(user); listResults(request, response); }
                case "GET /results/new" -> { staff(user); resultForm(request, response, null, null); }
                case "GET /results/edit" -> { staff(user); resultForm(request, response, findResult(request), null); }
                case "POST /results/create" -> { staff(user); saveResult(request, response, null); }
                case "POST /results/update" -> { staff(user); saveResult(request, response, findResultFromForm(request)); }
                case "POST /results/delete" -> { staff(user); results.delete(Validation.id(request.getParameter("id"))); success(request, response, "/results", "Result deleted successfully."); }
                case "GET /my-results" -> myResults(request, response, user);
                case "GET /report" -> report(request, response, user);
                default -> error(request, response, 404, "Page not found.");
            }
        } catch (Forbidden exception) {
            error(request, response, 403, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            error(request, response, 400, exception.getMessage());
        } catch (Exception exception) {
            getServletContext().log("Request failed", exception);
            error(request, response, 500, "Something went wrong. Please try again.");
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        User user = users.authenticate(email == null ? "" : email, password == null ? "" : password).orElse(null);
        if (user == null) {
            response.setStatus(401);
            request.setAttribute("error", "Invalid email or password.");
            view(request, response, "login");
            return;
        }
        HttpSession session = request.getSession(true);
        if (!session.isNew()) request.changeSessionId();
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("userId", user.id());
        session.setAttribute("passwordHash", users.passwordHash(user.id()).orElseThrow());
        session.setAttribute("csrf", token());
        redirect(request, response, "/dashboard");
    }

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long id)) return null;
        String storedHash = (String) session.getAttribute("passwordHash");
        if (storedHash == null || !users.passwordHash(id).filter(storedHash::equals).isPresent()) {
            session.invalidate();
            return null;
        }
        return users.findById(id).orElse(null);
    }

    private void dashboard(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        request.setAttribute("studentCount", results.countStudents());
        request.setAttribute("resultCount", user.role() == Role.STUDENT ? results.findByStudent(user.id()).size() : results.countResults());
        LocalDate today = LocalDate.now();
        int firstYear = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
        request.setAttribute("academicYear", firstYear + "–" + String.format("%02d", (firstYear + 1) % 100));
        view(request, response, "dashboard");
    }

    private void listStudents(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("students", users.findStudents());
        flash(request);
        view(request, response, "students");
    }

    private void listUsers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("users", users.findAll());
        flash(request);
        view(request, response, "users");
    }

    private void listResults(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String search = request.getParameter("q");
        request.setAttribute("search", search == null ? "" : search);
        request.setAttribute("results", results.findAll(search));
        flash(request);
        view(request, response, "results");
    }

    private void userForm(HttpServletRequest request, HttpServletResponse response, User edit, boolean studentOnly, String error) throws ServletException, IOException {
        Map<String, String> values = new HashMap<>();
        if (edit != null) {
            values.put("fullName", edit.fullName());
            values.put("email", edit.email());
            values.put("rollNumber", edit.rollNumber() == null ? "" : edit.rollNumber());
            values.put("role", edit.role().name());
        } else values.put("role", studentOnly ? "STUDENT" : "TEACHER");
        if (error != null) {
            for (String name : List.of("fullName", "email", "rollNumber", "role")) {
                String submitted = request.getParameter(name);
                if (submitted != null) values.put(name, submitted);
            }
            response.setStatus(400);
        }
        request.setAttribute("edit", edit);
        request.setAttribute("isEdit", edit != null);
        request.setAttribute("studentOnly", studentOnly);
        request.setAttribute("values", values);
        request.setAttribute("error", error);
        view(request, response, "user-form");
    }

    private void saveUser(HttpServletRequest request, HttpServletResponse response, User edit, boolean studentOnly) throws ServletException, IOException {
        Map<String, String> form = form(request, "fullName", "email", "password", "rollNumber", "role");
        if (studentOnly) form.put("role", "STUDENT");
        try {
            Validation.user(form, edit == null);
            Role role = Role.valueOf(form.get("role"));
            if (edit != null) {
                User actor = (User) request.getAttribute("user");
                if (edit.id() == actor.id() && role != Role.ADMIN) throw new IllegalArgumentException("You cannot remove your own admin access.");
                if (edit.role() == Role.STUDENT && role != Role.STUDENT && !results.findByStudent(edit.id()).isEmpty())
                    throw new IllegalArgumentException("Remove this student's results before changing their role.");
                users.update(edit.id(), form.get("email"), form.get("fullName"), role, form.get("rollNumber"), form.get("password"));
                if (edit.id() == actor.id() && !form.get("password").isBlank()) {
                    request.getSession(false).invalidate();
                    redirect(request, response, "/login");
                    return;
                }
            } else users.create(form.get("email"), form.get("password"), form.get("fullName"), role, form.get("rollNumber"));
            success(request, response, studentOnly ? "/students" : "/users", (studentOnly ? "Student" : "User") + (edit == null ? " created" : " updated") + " successfully.");
        } catch (IllegalArgumentException exception) {
            userForm(request, response, edit, studentOnly, exception.getMessage());
        }
    }

    private void resultForm(HttpServletRequest request, HttpServletResponse response, Result edit, String error) throws ServletException, IOException {
        Map<String, String> values = new HashMap<>();
        if (edit != null) {
            values.put("studentId", String.valueOf(edit.studentId()));
            values.put("subject", edit.subject());
            values.put("examName", edit.examName());
            values.put("marks", String.valueOf(edit.marks()));
            values.put("maxMarks", String.valueOf(edit.maxMarks()));
            values.put("academicYear", edit.academicYear());
        } else {
            values.put("examName", "Semester 1");
            values.put("maxMarks", "100");
            LocalDate today = LocalDate.now();
            int year = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
            values.put("academicYear", year + "-" + String.format("%02d", (year + 1) % 100));
        }
        if (error != null) {
            for (String name : List.of("studentId", "subject", "examName", "marks", "maxMarks", "academicYear")) {
                String submitted = request.getParameter(name);
                if (submitted != null) values.put(name, submitted);
            }
            response.setStatus(400);
        }
        request.setAttribute("edit", edit);
        request.setAttribute("isEdit", edit != null);
        request.setAttribute("values", values);
        request.setAttribute("students", users.findStudents());
        request.setAttribute("error", error);
        view(request, response, "result-form");
    }

    private void saveResult(HttpServletRequest request, HttpServletResponse response, Result edit) throws ServletException, IOException {
        Map<String, String> form = form(request, "studentId", "subject", "marks", "maxMarks", "examName", "academicYear");
        try {
            Validation.result(form);
            long studentId = Validation.id(form.get("studentId"));
            User student = users.findById(studentId).orElseThrow(() -> new IllegalArgumentException("Student not found."));
            student(student);
            if (edit == null) results.create(studentId, form.get("subject"), Validation.number(form.get("marks")), Validation.number(form.get("maxMarks")), form.get("examName"), form.get("academicYear"));
            else results.update(edit.id(), studentId, form.get("subject"), Validation.number(form.get("marks")), Validation.number(form.get("maxMarks")), form.get("examName"), form.get("academicYear"));
            success(request, response, "/results", "Result " + (edit == null ? "created" : "updated") + " successfully.");
        } catch (IllegalArgumentException exception) {
            resultForm(request, response, edit, exception.getMessage());
        }
    }

    private void myResults(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        if (user.role() != Role.STUDENT) { redirect(request, response, "/results"); return; }
        request.setAttribute("results", results.findByStudent(user.id()));
        view(request, response, "my-results");
    }

    private void report(HttpServletRequest request, HttpServletResponse response, User user) throws ServletException, IOException {
        Result selected = findResult(request);
        if (user.role() == Role.STUDENT && selected.studentId() != user.id()) throw new Forbidden("You can view only your own report cards.");
        List<Result> grouped = results.findByStudent(selected.studentId()).stream()
                .filter(item -> item.examName().equals(selected.examName()) && item.academicYear().equals(selected.academicYear()))
                .collect(Collectors.toList());
        long obtained = grouped.stream().mapToLong(Result::marks).sum();
        long maximum = grouped.stream().mapToLong(Result::maxMarks).sum();
        LocalDateTime updated = grouped.stream().map(Result::updatedAt).max(LocalDateTime::compareTo).orElse(selected.updatedAt());
        request.setAttribute("selected", selected);
        request.setAttribute("grouped", grouped);
        request.setAttribute("obtained", obtained);
        request.setAttribute("maximum", maximum);
        request.setAttribute("percentage", String.format("%.1f%%", maximum == 0 ? 0 : obtained * 100.0 / maximum));
        request.setAttribute("overallStatus", grouped.stream().allMatch(item -> item.status().equals("Pass")) ? "Passed" : "Needs improvement");
        request.setAttribute("updated", updated.format(DATE_TIME));
        request.setAttribute("studentInitials", initials(selected.studentName()));
        view(request, response, "report");
    }

    private User findUser(HttpServletRequest request) {
        return users.findById(Validation.id(request.getParameter("id"))).orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private User findUserFromForm(HttpServletRequest request) { return findUser(request); }
    private Result findResult(HttpServletRequest request) {
        return results.findById(Validation.id(request.getParameter("id"))).orElseThrow(() -> new IllegalArgumentException("Result not found."));
    }
    private Result findResultFromForm(HttpServletRequest request) { return findResult(request); }
    private void staff(User user) { if (!user.isStaff()) throw new Forbidden("Staff access is required."); }
    private void admin(User user) { if (user.role() != Role.ADMIN) throw new Forbidden("Admin access is required."); }
    private void student(User user) { if (user.role() != Role.STUDENT) throw new Forbidden("Only student accounts can be changed here."); }

    private Map<String, String> form(HttpServletRequest request, String... names) {
        Map<String, String> values = new HashMap<>();
        for (String name : names) values.put(name, request.getParameter(name) == null ? "" : request.getParameter(name));
        return values;
    }

    private void flash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        request.setAttribute("message", session.getAttribute("flash"));
        session.removeAttribute("flash");
    }

    private void success(HttpServletRequest request, HttpServletResponse response, String path, String message) throws IOException {
        request.getSession(false).setAttribute("flash", message);
        redirect(request, response, path);
    }

    private void error(HttpServletRequest request, HttpServletResponse response, int status, String message) throws ServletException, IOException {
        response.setStatus(status);
        request.setAttribute("status", status);
        request.setAttribute("error", message);
        view(request, response, "error");
    }

    private void view(HttpServletRequest request, HttpServletResponse response, String name) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/" + name + ".jsp").forward(request, response);
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", request.getContextPath() + path);
    }

    private String token() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String firstName(String name) { return name == null || name.isBlank() ? "there" : name.trim().split("\\s+")[0]; }
    private String initials(String name) {
        if (name == null || name.isBlank()) return "SR";
        String[] parts = name.trim().split("\\s+");
        return ("" + parts[0].charAt(0) + (parts.length > 1 ? parts[parts.length - 1].charAt(0) : "")).toUpperCase();
    }

    private static final class Forbidden extends RuntimeException {
        private Forbidden(String message) { super(message); }
    }
}
