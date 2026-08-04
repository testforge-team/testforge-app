package com.testforge.result.dto;

import lombok.Data;

/** One answer inside a submission: which question, which option the student picked. */
@Data
public class AnswerDto {
    private Long questionId;
    private String selectedOption;   // "A".."D", or null/absent if skipped
}
