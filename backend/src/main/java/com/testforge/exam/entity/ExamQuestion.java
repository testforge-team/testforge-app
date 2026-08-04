package com.testforge.exam.entity;

import com.testforge.question.entity.Question;
import jakarta.persistence.*;
import lombok.*;

/**
 * One row of 'exam_questions': "this question is part of that exam".
 * This is the JUNCTION TABLE that makes the many-to-many relationship
 * possible (an exam has many questions; a question can be reused in
 * many exams).
 *
 * How the three annotations cooperate:
 *  - @EmbeddedId  : the primary key of this entity is the ExamQuestionId pair.
 *  - @ManyToOne   : real object links to the Exam and Question rows.
 *  - @MapsId(...) : "the exam relation FILLS the examId half of the key,
 *                    and the question relation fills the questionId half" —
 *                    so key and relations always stay in sync automatically.
 */
@Entity
@Table(name = "exam_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamQuestion {

    @EmbeddedId
    private ExamQuestionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("examId")                       // ties to ExamQuestionId.examId
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")                   // ties to ExamQuestionId.questionId
    @JoinColumn(name = "question_id")
    private Question question;
}
