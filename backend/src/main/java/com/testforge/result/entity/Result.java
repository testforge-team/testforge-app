package com.testforge.result.entity;

import com.testforge.exam.entity.Exam;
import com.testforge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row of 'results' = ONE completed exam attempt by ONE student.
 *
 * Why link to the exam and user as objects (not raw ids)?
 * Because then we can write result.getExam().getPassingMarks() and
 * result.getUser().getName() directly in code, and Hibernate handles
 * the foreign-key columns (exam_id, user_id) for us.
 *
 * Note: a student can take the same exam again -> that creates a SECOND
 * Result row with its own resultId. History is preserved; nothing is
 * overwritten. This is why student_answers link to a Result (an attempt),
 * not directly to a user.
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

    // Number of correct answers. Every question is worth 1 mark, so
    // finalScore is simply the count of correct answers.
    @Column(name = "final_score", nullable = false)
    private Integer finalScore;

    // When the student submitted. Set automatically on insert.
    @Column(name = "exam_date", nullable = false, updatable = false)
    private LocalDateTime examDate;

    @PrePersist
    protected void onCreate() {
        this.examDate = LocalDateTime.now();
    }
}
