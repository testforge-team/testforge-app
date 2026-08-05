package com.testforge.result.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * NEW FILE.
 * The body of the auto-save call. Sent every time the student clicks an
 * option, so the server always holds the latest answers.
 *
 * selectedOption may be null, which means the student cleared their choice.
 */
@Data
public class SaveAnswerRequest {

    @NotNull(message = "questionId is required")
    private Long questionId;

    private String selectedOption;   // "A".."D", or null to clear
}
