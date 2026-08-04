package com.testforge.email.controller;

import com.testforge.email.dto.EmailLogDto;
import com.testforge.email.entity.EmailLog;
import com.testforge.email.repository.EmailLogRepository;
import com.testforge.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only reminder controls: trigger a broadcast, and view the log.
 */
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmailController {

    private final EmailService emailService;
    private final EmailLogRepository emailLogRepository;

    /** Manually broadcast a reminder for one exam to all students. */
    @PostMapping("/send/{examId}")
    public ResponseEntity<Map<String, Object>> send(@PathVariable Long examId) {
        int sent = emailService.broadcastReminder(examId);
        return ResponseEntity.ok(Map.of("message", "Reminders processed", "sent", sent));
    }

    /** The admin's reminder log table. */
    @GetMapping("/logs")
    @Transactional(readOnly = true)
    public ResponseEntity<List<EmailLogDto>> logs() {
        List<EmailLogDto> out = emailLogRepository.findAllByOrderByLogIdDesc().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(out);
    }

    private EmailLogDto toDto(EmailLog log) {
        return EmailLogDto.builder()
                .logId(log.getLogId())
                .studentEmail(log.getUser().getEmail())
                .examTitle(log.getExam().getTitle())
                .status(log.getStatus().name())
                .sentAt(log.getSentAt())
                .build();
    }
}
