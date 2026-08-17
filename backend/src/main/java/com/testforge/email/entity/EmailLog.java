package com.testforge.email.entity;

import com.testforge.common.enums.EmailStatus;
import com.testforge.exam.entity.Exam;
import com.testforge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row of 'email_logs' = one reminder email attempt.
 * It is the permanent audit trail: whether each reminder was SENT or FAILED.
 * A row is written EITHER WAY, so the admin can always see what happened.
 */
@Entity
@Table(name = "email_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    // Stored as text ("SENT"/"FAILED"/"PENDING"), matching the DB CHECK.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EmailStatus status;

    // NULL until the email actually goes out. Nullable on purpose:
    // a FAILED or PENDING row has no successful send time.
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
