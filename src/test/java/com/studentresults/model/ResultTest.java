package com.studentresults.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultTest {
    private Result result(int marks) {
        return new Result(1, 2, "Riya", "SRM-001", "Math", marks, 100,
                "Semester 1", "2025-26", LocalDateTime.now());
    }

    @Test
    void calculatesPercentageGradeAndPassStatus() {
        assertEquals(92.0, result(92).percentage());
        assertEquals("A+", result(92).grade());
        assertEquals("Pass", result(40).status());
        assertEquals("Fail", result(39).status());
    }

    @Test
    void coversGradeBoundaries() {
        assertEquals("A", result(80).grade());
        assertEquals("B", result(70).grade());
        assertEquals("C", result(60).grade());
        assertEquals("D", result(50).grade());
        assertEquals("E", result(49).grade());
        assertEquals("E", result(40).grade());
        assertEquals("F", result(39).grade());
    }
}
