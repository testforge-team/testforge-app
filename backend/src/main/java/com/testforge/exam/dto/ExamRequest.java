package com.testforge.exam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/** What the admin sends to create or update an exam. */
@Data
public class ExamRequest {

    @NotBlank(message = "Exam title is required")
    private String title;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Passing marks is required")
    @Min(value = 0, message = "Passing marks cannot be negative")
    private Integer passingMarks;

    // JSON like "2026-07-25T10:00:00" auto-converts to LocalDateTime.
    @NotNull(message = "Scheduled date/time is required")
    private LocalDateTime scheduledAt;
}
