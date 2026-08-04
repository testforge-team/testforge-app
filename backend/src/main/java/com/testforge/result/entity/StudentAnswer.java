package com.testforge.result.entity;

import com.testforge.question.entity.Question;
import jakarta.persistence.*;
import lombok.*;

/**
 * One row of 'student_answers' = the student's answer to ONE question
 * within ONE attempt. There is one row per question of the exam.
 *
 * This is the granular record that powers everything downstream:
 * Person D's weakness analytics reads these rows, grouped by the
 * question's topic, to find which topics the student gets wrong.
 */
@Entity
@Table(name = "student_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;

    // Which attempt this answer belongs to (links back to the Result row).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private Result result;

    // Which question was being answered.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // "A"/"B"/"C"/"D", or NULL if the student skipped the question.
    @Column(name = "selected_option", length = 1)
    private String selectedOption;

    // Stored directly (denormalized) so analytics queries are fast:
    // we can count wrong answers without re-checking the answer key.
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
}
