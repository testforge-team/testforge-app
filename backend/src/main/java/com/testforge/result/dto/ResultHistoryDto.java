package com.testforge.result.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One row of a student's past-exams list. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultHistoryDto {
    private Long resultId;
    private String examTitle;
    private int obtainedMarks;
    private int totalMarks;
    private double percentage;
    private String status;
    private LocalDateTime examDate;
}
