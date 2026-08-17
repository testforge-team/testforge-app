package com.testforge.email.service;

import com.testforge.exam.entity.Exam;
import com.testforge.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The automatic 24-hour reminder job (runs every 15 minutes).
 *
 * CHANGED FROM THE FIRST VERSION:
 * The old version skipped an exam entirely if ANY row existed in email_logs -
 * which meant a FAILED send permanently blocked retries. Now the dedup lives
 * INSIDE EmailService.broadcastReminderSkippingSent(), per student:
 *
 *   - student with a SENT row     -> skipped forever (no duplicates)
 *   - student with only FAILED    -> retried every 15 min until it succeeds
 *   - student with no row at all  -> sent (e.g. registered after the 1st run)
 *
 * So we call the broadcast on EVERY run for every exam in the 24h window,
 * and trust the per-student check to keep it idempotent.
 */
@Component
@RequiredArgsConstructor
public class ExamReminderScheduler {

    private final ExamRepository examRepository;
    private final EmailService emailService;

    /** Every 15 minutes, at second 0. */
    @Scheduled(cron = "0 */30 * * * *") //@Scheduled(cron = "0 */15 * * * *")
    public void sendUpcomingExamReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusHours(24);
        

        // Exams that start within the next 24 hours.
        List<Exam> upcoming = examRepository.findByScheduledAtBetween(now, windowEnd);

        for (Exam exam : upcoming) {
            try {
                int sent = emailService.broadcastReminderSkippingSent(exam.getExamId());
                if (sent > 0) {
                    System.out.println("[ReminderScheduler] Exam " + exam.getExamId()
                            + " (\"" + exam.getTitle() + "\"): sent " + sent
                            + " reminder(s) this run.");
                }
            } catch (Exception e) {
                // One bad exam must not stop reminders for the others.
                System.err.println("[ReminderScheduler] Failed for exam "
                        + exam.getExamId() + ": " + e.getMessage());
            }
        }
    }
}
