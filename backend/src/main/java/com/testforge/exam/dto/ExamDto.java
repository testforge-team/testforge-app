package com.testforge.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Exam summary for the admin table and the student dashboard.
 *
 * NEW FIELDS IN THIS VERSION:
 *   activeHours - how long the exam stays open after it starts
 *   endsAt      - the closing moment (scheduledAt + activeHours), calculated
 *   status      - NOT_STARTED / ACTIVE / EXPIRED, calculated fresh every time
 *
 * status is what the toggle on the screen shows. We calculate it here rather
 * than storing it in the database, so it is always correct without any job
 * having to update rows.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamDto {
    private Long examId;
    private String title;
    private Integer durationMinutes;
    private Integer passingMarks;
    private LocalDateTime scheduledAt;
    private long totalQuestions;

    private Integer activeHours;
    private LocalDateTime endsAt;
    private String status;          // "NOT_STARTED" | "ACTIVE" | "EXPIRED"
}
