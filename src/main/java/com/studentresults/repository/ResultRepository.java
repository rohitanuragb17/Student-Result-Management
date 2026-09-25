package com.studentresults.repository;

import com.studentresults.database.Database;
import com.studentresults.model.Result;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ResultRepository {
    private static final String SELECT = """
            SELECT r.*, u.full_name, u.roll_number
            FROM results r JOIN users u ON r.student_id = u.id
            """;

    private final Database database;

    public ResultRepository(Database database) {
        this.database = database;
    }

    public List<Result> findAll(String search) {
        String value = search == null ? "" : search.trim().toLowerCase();
        String pattern = "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        String sql = SELECT + """
                WHERE LOWER(u.full_name) LIKE ? ESCAPE '!' OR LOWER(u.roll_number) LIKE ? ESCAPE '!'
                   OR LOWER(r.subject) LIKE ? ESCAPE '!' OR LOWER(r.exam_name) LIKE ? ESCAPE '!'
                   OR LOWER(r.academic_year) LIKE ? ESCAPE '!'
                ORDER BY r.academic_year DESC, r.exam_name, u.full_name, r.subject
                """;
        List<Result> results = new ArrayList<>();
        try (var connection = database.connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 5; index++) statement.setString(index, pattern);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) results.add(map(rows));
            }
            return results;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load results", exception);
        }
    }

    public List<Result> findByStudent(long studentId) {
        String sql = SELECT + " WHERE r.student_id = ? ORDER BY r.academic_year DESC, r.exam_name, r.subject";
        return query(sql, studentId);
    }

    public Optional<Result> findById(long id) {
        return query(SELECT + " WHERE r.id = ?", id).stream().findFirst();
    }

    public void create(long studentId, String subject, int marks, int maxMarks, String examName, String academicYear) {
        String sql = "INSERT INTO results (student_id, subject, marks, max_marks, exam_name, academic_year) VALUES (?, ?, ?, ?, ?, ?)";
        save(sql, 0, studentId, subject, marks, maxMarks, examName, academicYear);
    }

    public void update(long id, long studentId, String subject, int marks, int maxMarks, String examName, String academicYear) {
        String sql = "UPDATE results SET student_id = ?, subject = ?, marks = ?, max_marks = ?, exam_name = ?, academic_year = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        save(sql, id, studentId, subject, marks, maxMarks, examName, academicYear);
    }

    public void delete(long id) {
        try (var connection = database.connection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM results WHERE id = ?")) {
            statement.setLong(1, id);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Result not found.");
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not delete result", exception);
        }
    }

    public int countStudents() {
        return count("SELECT COUNT(*) FROM users WHERE role = 'STUDENT'");
    }

    public int countResults() {
        return count("SELECT COUNT(*) FROM results");
    }

    private List<Result> query(String sql, long id) {
        List<Result> results = new ArrayList<>();
        try (var connection = database.connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) results.add(map(rows));
            }
            return results;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load results", exception);
        }
    }

    private void save(String sql, long id, long studentId, String subject, int marks, int maxMarks,
                      String examName, String academicYear) {
        subject = subject.trim();
        examName = examName.trim();
        academicYear = academicYear.trim();
        try (var connection = database.connection()) {
            String duplicateSql = """
                    SELECT id FROM results WHERE student_id = ?
                    AND LOWER(TRIM(subject)) = LOWER(?) AND LOWER(TRIM(exam_name)) = LOWER(?)
                    AND academic_year = ? AND id <> ?
                    """;
            try (PreparedStatement duplicate = connection.prepareStatement(duplicateSql)) {
                duplicate.setLong(1, studentId);
                duplicate.setString(2, subject);
                duplicate.setString(3, examName);
                duplicate.setString(4, academicYear);
                duplicate.setLong(5, id);
                try (ResultSet rows = duplicate.executeQuery()) {
                    if (rows.next()) {
                        throw new IllegalArgumentException("A result for this student, subject, exam and year already exists.");
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, studentId);
                statement.setString(2, subject);
                statement.setInt(3, marks);
                statement.setInt(4, maxMarks);
                statement.setString(5, examName);
                statement.setString(6, academicYear);
                if (id > 0) statement.setLong(7, id);
                if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Result not found.");
            }
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new IllegalArgumentException("A result for this student, subject, exam and year already exists.");
            }
            throw new IllegalStateException("Could not save result", exception);
        }
    }

    private int count(String sql) {
        try (var connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet row = statement.executeQuery()) {
            row.next();
            return row.getInt(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not count records", exception);
        }
    }

    private Result map(ResultSet row) throws SQLException {
        return new Result(
                row.getLong("id"), row.getLong("student_id"), row.getString("full_name"),
                row.getString("roll_number"), row.getString("subject"), row.getInt("marks"),
                row.getInt("max_marks"), row.getString("exam_name"), row.getString("academic_year"),
                row.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
