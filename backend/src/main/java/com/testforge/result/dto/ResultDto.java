package com.testforge.result.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The instant verdict returned right after submission — exactly the four
 * numbers the Result wireframe shows. Percentage and status are COMPUTED
 * here (not stored), from finalScore, total questions, and passingMarks.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultDto {
    private Long resultId;
    private int totalMarks;       // = number of questions in the exam
    private int obtainedMarks;    // = finalScore
    private double percentage;    // obtained / total * 100
    private String status;        // "PASS" or "FAIL"
}
