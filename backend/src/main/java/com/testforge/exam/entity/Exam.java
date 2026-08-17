package com.testforge.exam.entity;

import com.testforge.common.enums.ExamStatus;
import com.testforge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row of 'exams': the SETTINGS of a test.
 *
 * WHAT IS NEW IN THIS VERSION:
 *   activeHours  - how long the exam stays open after scheduledAt.
 *                  So the exam window is:
 *                       opens  at  scheduledAt
 *                       closes at  scheduledAt + activeHours
 *   getEndsAt()  - the closing moment, calculated not stored.
 *   getStatus()  - NOT_STARTED / ACTIVE / EXPIRED, also calculated.
 *
 * We calculate these instead of storing them because a stored value would
 * become wrong the moment the admin edits the schedule, and it would need a
 * background job just to keep it correct.
 */
@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Long examId;

    @Column(nullable = false, length = 150)
    private String title;

    /** How many minutes ONE student gets once they press Start. */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /** Minimum score needed to pass. Person C's grading compares against this. */
    @Column(name = "passing_marks", nullable = false)
    private Integer passingMarks;

    /** When the exam OPENS. */
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    /**
     * NEW: how many hours the exam stays open after it opens.
     * Default 24 keeps every existing exam behaving sensibly.
     */
    @Column(name = "active_hours", nullable = false)
    private Integer activeHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // Safety net: if an older code path forgets to set it, use 24 hours.
        if (this.activeHours == null) {
            this.activeHours = 24;
        }
    }

    // ------------------------------------------------------------
    // Calculated helpers (not columns - no @Column annotation)
    // ------------------------------------------------------------

    /** The exact moment this exam closes for everyone. */
    @Transient
    public LocalDateTime getEndsAt() {
        int hours = (activeHours == null) ? 24 : activeHours;
        return scheduledAt.plusHours(hours);
    }

    /** Where the exam is in its timeline, right now. */
    @Transient
    public ExamStatus getStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(scheduledAt))   return ExamStatus.NOT_STARTED;
        if (now.isAfter(getEndsAt()))    return ExamStatus.EXPIRED;
        return ExamStatus.ACTIVE;
    }

    /** Convenience used by the service before allowing a new attempt. */
    @Transient
    public boolean isOpenNow() {
        return getStatus() == ExamStatus.ACTIVE;
    }
}
