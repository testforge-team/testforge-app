package com.testforge.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One topic in a student's strengths/weaknesses list. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeakTopicDto {
    private String topicName;
    private double accuracy;   // 0-100 across all this student's answers in the topic
    private String status;     // "NEEDS_REVISION" or "MASTERED"
}
