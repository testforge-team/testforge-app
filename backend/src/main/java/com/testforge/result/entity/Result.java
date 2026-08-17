package com.testforge.result.entity;

import com.testforge.common.enums.AttemptStatus;
import com.testforge.exam.entity.Exam;
import com.testforge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row of 'results' = ONE attempt at an exam.
 *
 * WHAT CHANGED IN THIS VERSION:
 * Previously this row was created only when the student SUBMITTED. Now it is
 * created the moment the student presses START, and it lives through the
 * attempt. That single change is what makes the exam survive a page refresh:
 * the attempt exists on the server, so the browser can always ask "where was
 * I?" and get a truthful answer.
 *
 * New fields:
 *   startedAt - when Start was pressed
 *   endTime   - the exact moment this attempt must close. THE TIMER IS
 *               DERIVED FROM THIS, not counted down by the browser. So a
 *               refresh cannot give the student extra time.
 *   status    - IN_PROGRESS while writing, then SUBMITTED / AUTO_SUBMITTED /
 *               EXPIRED once finished.
 */
@Entity
@Table(name = "results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /** Number of correct answers. Stays 0 until the attempt is graded. */
    @Column(name = "final_score", nullable = false)
    private Integer finalScore;

    /**
     * Set when the attempt STARTS, then updated to the real submission time
     * when it is graded. Existing screens that show "exam date" keep working.
     */
    @Column(name = "exam_date", nullable = false)
    private LocalDateTime examDate;

    /** NEW: when the student pressed Start. */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /** NEW: the deadline for this attempt, decided by the server. */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** NEW: the life cycle stage of this attempt. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.examDate == null)  this.examDate = now;
        if (this.startedAt == null) this.startedAt = now;
        if (this.status == null)    this.status = AttemptStatus.IN_PROGRESS;
        if (this.finalScore == null) this.finalScore = 0;
    }

    /** True while the student is still writing this attempt. */
    @Transient
    public boolean isInProgress() {
        return status == AttemptStatus.IN_PROGRESS;
    }

    /** True if the deadline has already passed. */
    @Transient
    public boolean isTimeOver() {
        return endTime != null && LocalDateTime.now().isAfter(endTime);
    }

    /** Seconds left, never negative. This is what the browser shows. */
    @Transient
    public long getRemainingSeconds() {
        if (endTime == null) return 0;
        long secs = java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds();
        return Math.max(secs, 0);
    }
}
