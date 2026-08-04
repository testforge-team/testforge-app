package com.testforge.exam.entity;

import com.testforge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row of 'exams': the SETTINGS of a test (not its questions —
 * those are linked through the exam_questions junction table).
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

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    // Minimum final_score needed to PASS (each question = 1 mark).
    // Person C's GradingService reads this to compute the status.
    @Column(name = "passing_marks", nullable = false)
    private Integer passingMarks;

    // When the exam takes place. Person D's reminder job reads this
    // to know which exams are ~24 hours away.
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    // FK to users: which admin created this exam (accountability).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
