package com.testforge.result.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Per-topic score inside one attempt, for the result screen's breakdown. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicBreakdownDto {
    private String topicName;
    private int correct;
    private int total;
    private double accuracy;   // correct / total * 100
}
