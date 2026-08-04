package com.testforge.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The STUDENT view of a question during an exam attempt.
 * There is deliberately NO correctOption field in this class —
 * so even a bug cannot leak the answer key mid-exam; the field
 * simply does not exist in the JSON. This is Person B's #1 security rule.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionStudentDto {
    private Long questionId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String topicName;   // shown as "Topic: SQL" on the exam screen
}
