package com.testforge.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** What the admin sends when creating or editing a question. */
@Data
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotBlank(message = "Option A is required")
    private String optionA;

    @NotBlank(message = "Option B is required")
    private String optionB;

    @NotBlank(message = "Option C is required")
    private String optionC;

    @NotBlank(message = "Option D is required")
    private String optionD;

    // Regex guard: only the single letters A-D pass validation,
    // mirroring the CHECK constraint in the database.
    @NotBlank(message = "Correct option is required")
    @Pattern(regexp = "[ABCD]", message = "Correct option must be A, B, C or D")
    private String correctOption;

    @NotNull(message = "Topic id is required")
    private Long topicId;
}
