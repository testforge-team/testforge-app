package com.testforge.exam.repository;

import com.testforge.exam.entity.ExamQuestion;
import com.testforge.exam.entity.ExamQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Note the second type parameter: the id type is our composite key class,
 * not Long. That is the only unusual thing about this repository.
 */
@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, ExamQuestionId> {

    /** All mappings for one exam -> from these we get the exam's questions. */
    List<ExamQuestion> findByExam_ExamId(Long examId);

    /** How many questions in this exam = the exam's TOTAL MARKS (1 mark each). */
    long countByExam_ExamId(Long examId);

    /** Used to skip duplicates when the admin maps questions. */
    boolean existsById(ExamQuestionId id);
}
