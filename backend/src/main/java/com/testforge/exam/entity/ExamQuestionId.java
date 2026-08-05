package com.testforge.exam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * THE COMPOSITE KEY CLASS — the trickiest 20 lines of Person B's week.
 *
 * The exam_questions table has NO single id column; its primary key is the
 * PAIR (exam_id, question_id). JPA represents a paired key as a separate
 * small class with exactly these rules:
 *
 *  1. @Embeddable            — "this class is a key that lives inside another entity"
 *  2. implements Serializable — required by the JPA specification for key classes
 *  3. a no-args constructor   — Hibernate builds it reflectively (Lombok provides it)
 *  4. equals() and hashCode() — Hibernate compares keys to know if two rows are
 *                               the same; Lombok's @EqualsAndHashCode generates
 *                               them from both fields
 *
 * Miss any one of the four and you get the classic errors:
 * "no default constructor" / "composite-id class must implement Serializable" /
 * duplicate rows silently appearing.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ExamQuestionId implements Serializable {

    private static final long serialVersionUID = 1L;

	@Column(name = "exam_id")
    private Long examId;

    @Column(name = "question_id")
    private Long questionId;
}
