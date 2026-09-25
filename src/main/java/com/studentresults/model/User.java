package com.studentresults.model;

public record User(
        long id,
        String email,
        String fullName,
        Role role,
        String rollNumber
) {
    public long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public String getRollNumber() { return rollNumber; }

    public boolean isStaff() {
        return role == Role.ADMIN || role == Role.TEACHER;
    }
}
