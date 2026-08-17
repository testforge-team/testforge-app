package com.testforge.result.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One line of the answer-sheet review. It is now safe to include
 * correctOption because the exam is OVER — revealing the key here helps
 * the student learn. (During the attempt it stayed hidden; see Person B.)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerReviewDto {
    private Long questionId;
    private String questionText;
    private String topicName;
    private String selectedOption;   // may be null (skipped)
    private String correctOption;
    private boolean correct;
}
