package com.studentresults.repository;

import com.studentresults.database.Database;
import com.studentresults.model.Role;
import com.studentresults.model.User;
import com.studentresults.security.PasswordUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UserRepository {
    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public Optional<User> authenticate(String email, String password) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (var connection = database.connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next() || !PasswordUtil.matches(password, row.getString("password_salt"), row.getString("password_hash"))) {
                    return Optional.empty();
                }
                return Optional.of(map(row));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not sign in", exception);
        }
    }

    public List<User> findAll() {
        return queryMany("SELECT * FROM users ORDER BY role, full_name", null);
    }

    public List<User> findStudents() {
        return queryMany("SELECT * FROM users WHERE role = 'STUDENT' ORDER BY full_name", null);
    }

    public Optional<User> findById(long id) {
        return queryMany("SELECT * FROM users WHERE id = ?", id).stream().findFirst();
    }

    public Optional<String> passwordHash(long id) {
        try (var connection = database.connection();
             PreparedStatement statement = connection.prepareStatement("SELECT password_hash FROM users WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? Optional.of(row.getString(1)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not check account session", exception);
        }
    }

    public long create(String email, String password, String fullName, Role role, String rollNumber) {
        String salt = PasswordUtil.newSalt();
        String sql = "INSERT INTO users (email, password_hash, password_salt, full_name, role, roll_number) VALUES (?, ?, ?, ?, ?, ?)";
        try (var connection = database.connection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, email.trim().toLowerCase());
            statement.setString(2, PasswordUtil.hash(password, salt));
            statement.setString(3, salt);
            statement.setString(4, fullName);
            statement.setString(5, role.name());
            statement.setString(6, role == Role.STUDENT ? rollNumber : null);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        } catch (SQLException exception) {
            throw saveFailure(exception);
        }
    }

    public void update(long id, String email, String fullName, Role role, String rollNumber, String newPassword) {
        String sql = newPassword == null || newPassword.isBlank()
                ? "UPDATE users SET email = ?, full_name = ?, role = ?, roll_number = ? WHERE id = ?"
                : "UPDATE users SET email = ?, full_name = ?, role = ?, roll_number = ?, password_hash = ?, password_salt = ? WHERE id = ?";
        try (var connection = database.connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim().toLowerCase());
            statement.setString(2, fullName);
            statement.setString(3, role.name());
            statement.setString(4, role == Role.STUDENT ? rollNumber : null);
            if (newPassword == null || newPassword.isBlank()) {
                statement.setLong(5, id);
            } else {
                String salt = PasswordUtil.newSalt();
                statement.setString(5, PasswordUtil.hash(newPassword, salt));
                statement.setString(6, salt);
                statement.setLong(7, id);
            }
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("User not found.");
        } catch (SQLException exception) {
            throw saveFailure(exception);
        }
    }

    public void delete(long id) {
        try (var connection = database.connection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            statement.setLong(1, id);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("User not found.");
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not delete user", exception);
        }
    }

    private List<User> queryMany(String sql, Long id) {
        List<User> users = new ArrayList<>();
        try (var connection = database.connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (id != null) statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) users.add(map(rows));
            }
            return users;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load users", exception);
        }
    }

    private User map(ResultSet row) throws SQLException {
        return new User(
                row.getLong("id"), row.getString("email"), row.getString("full_name"),
                Role.valueOf(row.getString("role")), row.getString("roll_number")
        );
    }

    private RuntimeException saveFailure(SQLException exception) {
        if ("23505".equals(exception.getSQLState())) {
            return new IllegalArgumentException("Email or roll number already exists.");
        }
        return new IllegalStateException("Could not save user", exception);
    }
}
