package com.testforge.result.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * The body the student sends on submit. The examId comes from the URL
 * (/api/exams/{id}/submit), so the body only carries the list of answers.
 * Skipped questions may simply be omitted from the list.
 */
@Data
public class SubmitExamRequest {

    @NotNull(message = "Answers list is required (may be empty)")
    private List<AnswerDto> answers;
}
