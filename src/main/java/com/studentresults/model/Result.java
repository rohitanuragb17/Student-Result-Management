package com.studentresults.model;

import java.time.LocalDateTime;

public record Result(
        long id,
        long studentId,
        String studentName,
        String rollNumber,
        String subject,
        int marks,
        int maxMarks,
        String examName,
        String academicYear,
        LocalDateTime updatedAt
) {
    public long getId() { return id; }
    public long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getRollNumber() { return rollNumber; }
    public String getSubject() { return subject; }
    public int getMarks() { return marks; }
    public int getMaxMarks() { return maxMarks; }
    public String getExamName() { return examName; }
    public String getAcademicYear() { return academicYear; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public double getPercentage() { return percentage(); }
    public String getGrade() { return grade(); }
    public String getStatus() { return status(); }
    public String getPercentageLabel() { return String.format("%.1f%%", percentage()); }

    public double percentage() {
        return maxMarks == 0 ? 0 : marks * 100.0 / maxMarks;
    }

    public String grade() {
        double value = percentage();
        if (value >= 90) return "A+";
        if (value >= 80) return "A";
        if (value >= 70) return "B";
        if (value >= 60) return "C";
        if (value >= 50) return "D";
        if (value >= 40) return "E";
        return "F";
    }

    public String status() {
        return percentage() >= 40 ? "Pass" : "Fail";
    }
}
