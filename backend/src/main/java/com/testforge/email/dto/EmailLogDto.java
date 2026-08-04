package com.testforge.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One row of the admin's email-log table. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailLogDto {
    private Long logId;
    private String studentEmail;
    private String examTitle;
    private String status;
    private LocalDateTime sentAt;   // null if not sent
}
