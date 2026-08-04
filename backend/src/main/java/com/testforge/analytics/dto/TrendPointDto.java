package com.testforge.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One point on the student's performance-over-time line. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrendPointDto {
    private String examTitle;
    private double percentage;
    private LocalDateTime examDate;
}
