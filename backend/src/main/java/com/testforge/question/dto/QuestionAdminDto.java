package com.testforge.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The ADMIN view of a question: includes correctOption,
 * because admins manage the answer key.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionAdminDto {
    private Long questionId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;   // admin may see the key
    private Long topicId;
    private String topicName;
}
