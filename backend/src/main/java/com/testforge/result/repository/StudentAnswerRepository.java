package com.testforge.result.repository;

import com.testforge.result.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    /** All answers of one attempt - used for the review screen and grading. */
    List<StudentAnswer> findByResult_ResultId(Long resultId);

    /**
     * NEW - needed by the auto-save.
     * When the student changes their mind about a question we must UPDATE the
     * existing row, not insert a second one for the same question.
     */
    Optional<StudentAnswer> findByResult_ResultIdAndQuestion_QuestionId(
            Long resultId, Long questionId);
}
