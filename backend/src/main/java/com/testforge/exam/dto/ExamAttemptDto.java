package com.testforge.exam.dto;

import com.testforge.question.dto.QuestionStudentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Everything the student's exam screen needs to start an attempt:
 * the settings (for the timer) + the questions WITHOUT answers
 * (note the type: QuestionStudentDto — the class with no correctOption).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamAttemptDto {
    private Long examId;
    private String title;
    private Integer durationMinutes;
    private List<QuestionStudentDto> questions;
}
