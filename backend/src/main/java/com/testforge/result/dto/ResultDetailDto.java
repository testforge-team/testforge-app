package com.testforge.result.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Full detail of one past attempt: the headline + review + topic breakdown. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultDetailDto {
    private Long resultId;
    private String examTitle;
    private int obtainedMarks;
    private int totalMarks;
    private double percentage;
    private String status;
    private List<AnswerReviewDto> answers;
    private List<TopicBreakdownDto> topicBreakdown;
}
