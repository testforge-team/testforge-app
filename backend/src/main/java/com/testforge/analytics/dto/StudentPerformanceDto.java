package com.testforge.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** The whole "Performance Analysis" payload for one student. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentPerformanceDto {
    private List<TrendPointDto> trends;        // score over time
    private List<WeakTopicDto> strengths;      // high-accuracy topics
    private List<WeakTopicDto> weakTopics;     // low-accuracy topics to revise
}
