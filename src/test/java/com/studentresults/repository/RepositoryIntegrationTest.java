package com.studentresults.repository;

import com.studentresults.database.Database;
import com.studentresults.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoryIntegrationTest {
    private UserRepository users;
    private ResultRepository results;

    @BeforeEach
    void setUp() {
        Database database = new Database("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        database.initialize();
        users = new UserRepository(database);
        results = new ResultRepository(database);
    }

    @Test
    void authenticatesSeededAccountsButRejectsWrongPassword() {
        assertEquals(Role.ADMIN, users.authenticate("admin@school.com", "admin123").orElseThrow().role());
        assertTrue(users.authenticate("admin@school.com", "wrong-password").isEmpty());
    }

    @Test
    void createsUpdatesFindsAndDeletesPersistentRecords() {
        long studentId = users.create("kabir@school.com", "password123", "Kabir Singh", Role.STUDENT, "SRM-002");
        results.create(studentId, "History", 75, 100, "Semester 2", "2025-26");

        var created = results.findByStudent(studentId).getFirst();
        assertEquals("History", created.subject());
        assertEquals("B", created.grade());

        results.update(created.id(), studentId, "History", 85, 100, "Semester 2", "2025-26");
        assertEquals("A", results.findById(created.id()).orElseThrow().grade());

        results.delete(created.id());
        assertTrue(results.findByStudent(studentId).isEmpty());
    }

    @Test
    void deletingStudentCascadesToResults() {
        long studentId = users.create("meera@school.com", "password123", "Meera Joshi", Role.STUDENT, "SRM-003");
        results.create(studentId, "Art", 70, 100, "Annual", "2025-26");
        users.delete(studentId);
        assertTrue(results.findByStudent(studentId).isEmpty());
    }

    @Test
    void uniqueConstraintsRejectDuplicateRollAndSubjectRecord() {
        long studentId = users.create("first@school.com", "password123", "First", Role.STUDENT, "ROLL-1");
        assertThrows(IllegalArgumentException.class, () -> users.create("second@school.com", "password123", "Second", Role.STUDENT, "ROLL-1"));
        results.create(studentId, "Math", 70, 100, "Annual", "2025-26");
        assertThrows(IllegalArgumentException.class, () -> results.create(studentId, "Math", 80, 100, "Annual", "2025-26"));
        assertThrows(IllegalArgumentException.class, () -> results.create(studentId, " math ", 80, 100, "ANNUAL", "2025-26"));
        assertTrue(results.findAll("%").isEmpty());
        assertEquals(2, results.findAll("math").size());
    }
}
