package com.testforge.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The summary counts on the admin dashboard home. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardDto {
    private long totalStudents;
    private long totalExams;
    private long totalQuestions;
    private double batchAveragePercentage;
}
