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

    /** Minutes ONE student gets once they press Start. */
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Passing marks is required")
    @Min(value = 0, message = "Passing marks cannot be negative")
    private Integer passingMarks;

    /** When the exam OPENS. Format: 2026-08-10T17:00:00 */
    @NotNull(message = "Scheduled date/time is required")
    private LocalDateTime scheduledAt;

    /**
     * NEW: how many hours the exam stays open after it opens.
     * For example 24 means students may attempt it any time within a day of
     * the start time. Defaulted in the service if the client omits it, so old
     * requests keep working.
     */
    @Min(value = 1, message = "Active hours must be at least 1")
    private Integer activeHours;
}
