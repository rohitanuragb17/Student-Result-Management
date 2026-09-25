package com.studentresults.service;

import com.studentresults.model.Role;

import java.util.Map;

public final class Validation {
    private Validation() { }

    public static void user(Map<String, String> form, boolean passwordRequired) {
        required(form, "fullName", "Full name");
        required(form, "email", "Email");
        maxLength(form, "fullName", "Full name", 100);
        maxLength(form, "email", "Email", 120);
        maxLength(form, "rollNumber", "Roll number", 30);
        if (!form.get("email").matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        Role role;
        try {
            role = Role.valueOf(form.getOrDefault("role", ""));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Choose a valid role.");
        }
        if (role == Role.STUDENT) required(form, "rollNumber", "Roll number");
        String password = form.getOrDefault("password", "");
        if (passwordRequired || !password.isBlank()) {
            if (password.length() < 8) throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
    }

    public static void result(Map<String, String> form) {
        required(form, "studentId", "Student");
        required(form, "subject", "Subject");
        required(form, "marks", "Marks");
        required(form, "maxMarks", "Maximum marks");
        required(form, "examName", "Exam name");
        required(form, "academicYear", "Academic year");
        maxLength(form, "subject", "Subject", 80);
        maxLength(form, "examName", "Exam name", 80);
        int marks = positiveNumber(form.get("marks"), "Marks", true);
        int maxMarks = positiveNumber(form.get("maxMarks"), "Maximum marks", false);
        positiveNumber(form.get("studentId"), "Student", false);
        if (marks > maxMarks) throw new IllegalArgumentException("Marks cannot be greater than maximum marks.");
        if (!form.get("academicYear").matches("20\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Academic year must look like 2025-26.");
        }
        int firstYear = Integer.parseInt(form.get("academicYear").substring(0, 4));
        int ending = Integer.parseInt(form.get("academicYear").substring(5));
        if (ending != (firstYear + 1) % 100) {
            throw new IllegalArgumentException("Academic year must contain consecutive years, such as 2025-26.");
        }
    }

    public static long id(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid record id.");
        }
    }

    public static int number(String value) {
        return Integer.parseInt(value);
    }

    private static int positiveNumber(String value, String label, boolean zeroAllowed) {
        try {
            int number = Integer.parseInt(value);
            if (zeroAllowed ? number < 0 : number <= 0) throw new NumberFormatException();
            return number;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + (zeroAllowed ? " must be zero or more." : " must be greater than zero."));
        }
    }

    private static void required(Map<String, String> form, String key, String label) {
        if (form.getOrDefault(key, "").isBlank()) throw new IllegalArgumentException(label + " is required.");
    }

    private static void maxLength(Map<String, String> form, String key, String label, int limit) {
        if (form.getOrDefault(key, "").length() > limit) {
            throw new IllegalArgumentException(label + " must be " + limit + " characters or fewer.");
        }
    }
}
