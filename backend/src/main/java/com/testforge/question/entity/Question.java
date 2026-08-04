package com.testforge.question.entity;

import com.testforge.topic.entity.Topic;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row of the 'questions' table — the central question bank.
 * Questions live independently of exams: the same question can be
 * reused in many exams (via the exam_questions junction table).
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    /**
     * THE FOREIGN KEY, in Java form.
     * @ManyToOne = MANY questions belong to ONE topic.
     * @JoinColumn names the FK column in THIS table (questions.topic_id).
     * fetch = LAZY means: don't load the Topic from the DB until someone
     * actually calls getTopic() — faster, and the professional default.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // TEXT column in the DB -> columnDefinition, so long questions fit.
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "option_a", nullable = false)
    private String optionA;

    @Column(name = "option_b", nullable = false)
    private String optionB;

    @Column(name = "option_c", nullable = false)
    private String optionC;

    @Column(name = "option_d", nullable = false)
    private String optionD;

    // "A" | "B" | "C" | "D" — the answer key. NEVER expose to students
    // during an attempt (that's why we have two response DTOs).
    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
