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
 * JavaMailSender is provided automatically by Spring because we added the
 * 'spring-boot-starter-mail' dependency and set spring.mail.* in properties.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;

    /**
     * Broadcast a reminder for one exam to ALL registered students.
     * (Our design decision: no per-exam enrollment table, so reminders go
     * to every student.) Returns how many were sent successfully.
     */
    public int broadcastReminder(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        // Every STUDENT (admins don't take exams, so they're excluded).
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .toList();

        int sentCount = 0;
        for (User student : students) {
            if (sendOne(student, exam)) {
                sentCount++;
            }
        }
        return sentCount;
    }

    /**
     * Send to one student and log the outcome. Returns true on success.
     * The try/catch is the whole point: a failure to email ONE student
     * must not stop the loop or crash the request — we log FAILED and move on.
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
