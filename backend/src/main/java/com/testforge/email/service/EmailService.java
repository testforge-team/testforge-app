package com.testforge.email.service;

import com.testforge.common.enums.EmailStatus;
import com.testforge.common.enums.Role;
import com.testforge.email.entity.EmailLog;
import com.testforge.email.repository.EmailLogRepository;
import com.testforge.exam.entity.Exam;
import com.testforge.exam.repository.ExamRepository;
import com.testforge.exception.ResourceNotFoundException;
import com.testforge.user.entity.User;
import com.testforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Sends exam reminder emails and records every attempt in email_logs.
 *
 * TWO ENTRY POINTS NOW:
 *
 *   broadcastReminder(examId)
 *       The MANUAL path (admin presses the button). Sends to EVERY student,
 *       no questions asked - the admin explicitly wants a (re)send.
 *
 *   broadcastReminderSkippingSent(examId)
 *       The AUTOMATIC path (ExamReminderScheduler, every 15 min). Sends only
 *       to students who do NOT yet have a SENT log row for this exam. So:
 *         - a student already emailed successfully  -> skipped (no duplicate)
 *         - a student whose last attempt FAILED     -> retried
 *         - a student never attempted               -> sent
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;

    /**
     * MANUAL broadcast: send to ALL students unconditionally.
     * Returns how many were sent successfully.
     */
    public int broadcastReminder(Long examId) {
        Exam exam = getExam(examId);

        int sentCount = 0;
        for (User student : allStudents()) {
            if (sendOne(student, exam)) {
                sentCount++;
            }
        }
        return sentCount;
    }

    /**
     * AUTOMATIC broadcast: send only to students without a SENT row yet.
     * Because FAILED rows do not count, failed students are retried on every
     * scheduler run until they succeed (or the exam starts).
     * Returns how many were sent successfully THIS run.
     */
    public int broadcastReminderSkippingSent(Long examId) {
        Exam exam = getExam(examId);

        int sentCount = 0;
        for (User student : allStudents()) {

            // Already got the email successfully? Then never send again.
            boolean alreadySent = emailLogRepository
                    .existsByExam_ExamIdAndUser_UserIdAndStatus(
                            examId, student.getUserId(), EmailStatus.SENT);
            if (alreadySent) {
                continue;
            }

            if (sendOne(student, exam)) {
                sentCount++;
            }
        }
        return sentCount;
    }

    // ---------- shared helpers ----------

    private Exam getExam(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exam not found with id: " + examId));
    }

    /** Every STUDENT (admins don't take exams, so they're excluded). */
    private List<User> allStudents() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .toList();
    }

    /**
     * Send to one student and log the outcome. Returns true on success.
     * The try/catch is the whole point: a failure to email ONE student
     * must not stop the loop or crash the request - we log FAILED and move on.
     */
    private boolean sendOne(User student, Exam exam) {
        String when = exam.getScheduledAt()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(student.getEmail());
            message.setSubject("Reminder: " + exam.getTitle() + " is scheduled soon");
            message.setText("Hello " + student.getName() + ",\n\n"
                    + "This is a reminder that your exam \"" + exam.getTitle() + "\" "
                    + "is scheduled for " + when + ".\n\n"
                    + "Duration: " + exam.getDurationMinutes() + " minutes.\n\n"
                    + "Good luck!\nTestForge Team");
            mailSender.send(message);

            saveLog(student, exam, EmailStatus.SENT, LocalDateTime.now());
            return true;
        } catch (Exception e) {
            // Sending failed (bad address, SMTP down...). Record it, keep going.
            e.printStackTrace();
            saveLog(student, exam, EmailStatus.FAILED, null);
            return false;
        }
    }

    private void saveLog(User user, Exam exam, EmailStatus status, LocalDateTime sentAt) {
        emailLogRepository.save(EmailLog.builder()
                .user(user)
                .exam(exam)
                .status(status)
                .sentAt(sentAt)
                .build());
    }
}
