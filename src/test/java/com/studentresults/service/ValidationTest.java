package com.studentresults.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationTest {
    @Test
    void acceptsValidEmailButRejectsInvalidEmail() {
        Map<String, String> valid = new HashMap<>(Map.of(
                "fullName", "Riya Sharma", "email", "riya@school.com", "password", "password123",
                "role", "STUDENT", "rollNumber", "SRM-001"
        ));
        assertDoesNotThrow(() -> Validation.user(valid, true));
        valid.put("email", "not-an-email");
        assertThrows(IllegalArgumentException.class, () -> Validation.user(valid, true));
    }

    @Test
    void acceptsValidResult() {
        assertDoesNotThrow(() -> Validation.result(validResult()));
    }

    @Test
    void rejectsMarksAboveMaximum() {
        Map<String, String> form = new HashMap<>(validResult());
        form.put("marks", "101");
        assertThrows(IllegalArgumentException.class, () -> Validation.result(form));
    }

    @Test
    void rejectsInvalidAcademicYear() {
        Map<String, String> form = new HashMap<>(validResult());
        form.put("academicYear", "2025");
        assertThrows(IllegalArgumentException.class, () -> Validation.result(form));
        form.put("academicYear", "2025-99");
        assertThrows(IllegalArgumentException.class, () -> Validation.result(form));
    }

    @Test
    void rejectsMissingFieldsAndOversizedValues() {
        Map<String, String> form = new HashMap<>(validResult());
        form.remove("subject");
        assertThrows(IllegalArgumentException.class, () -> Validation.result(form));
        form.put("subject", "A".repeat(81));
        assertThrows(IllegalArgumentException.class, () -> Validation.result(form));
        form.put("subject", "Math");
        form.put("maxMarks", "0");
        assertThrows(IllegalArgumentException.class, () -> Validation.result(form));
    }

    private Map<String, String> validResult() {
        return Map.of(
                "studentId", "1", "subject", "Math", "marks", "88", "maxMarks", "100",
                "examName", "Semester 1", "academicYear", "2025-26"
        );
    }
}
