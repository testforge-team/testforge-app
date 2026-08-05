package com.testforge.result.repository;

import com.testforge.common.enums.AttemptStatus;
import com.testforge.result.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    /**
     * A student's attempt history, newest first.
     * NOTE: this now also returns IN_PROGRESS attempts, so the history screen
     * filters them out - see ResultService.getHistory().
     */
    List<Result> findByUser_UserIdOrderByExamDateDesc(Long userId);

    /**
     * NEW - the heart of the resume feature.
     * Is this student already in the middle of this exam? If yes we continue
     * that attempt instead of creating a new one, which is exactly what makes
     * a page refresh harmless.
     */
    Optional<Result> findByUser_UserIdAndExam_ExamIdAndStatus(
            Long userId, Long examId, AttemptStatus status);

    /**
     * NEW - used by the background job.
     * Finds attempts that are still marked IN_PROGRESS even though their
     * deadline has passed. These belong to students who closed the tab and
     * never came back, so nobody is left to submit for them.
     */
    List<Result> findByStatusAndEndTimeBefore(AttemptStatus status, LocalDateTime cutoff);

    /** Only finished attempts, for history and analytics. */
    List<Result> findByUser_UserIdAndStatusNotOrderByExamDateDesc(
            Long userId, AttemptStatus status);
}
