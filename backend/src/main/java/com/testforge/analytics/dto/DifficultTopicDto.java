package com.testforge.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of the admin's "Difficult Topics" report (whole batch). */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DifficultTopicDto {
    private String topicName;
    private long totalAnswers;
    private double failureRate;   // wrong / total * 100, across all students
}
