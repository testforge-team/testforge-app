package com.testforge.topic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Used BOTH as the request body (client sends { "topicName": "SQL" })
 * and the response ( { "topicId": 3, "topicName": "SQL" } ).
 * Small modules can share one DTO; bigger ones (question, exam) split
 * request and response shapes.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicDto {

    private Long topicId;              // filled by the server in responses

    @NotBlank(message = "Topic name is required")
    private String topicName;
}
