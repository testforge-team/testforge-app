package com.testforge.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The summary counts on a student's dashboard home. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentDashboardDto {
    private long availableExams;
    private long examsTaken;
    private double averagePercentage;
    private long weakTopicsCount;
}
