package com.testforge.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Exam summary for lists (both admin table and student dashboard). */
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
    private long totalQuestions;   // count from exam_questions = total marks
}
