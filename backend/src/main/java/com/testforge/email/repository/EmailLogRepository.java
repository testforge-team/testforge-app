package com.testforge.email.repository;

import com.testforge.common.enums.EmailStatus;
import com.testforge.email.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    /** Newest log entries first, for the admin's reminder screen. */
    List<EmailLog> findAllByOrderByLogIdDesc();
   // boolean existsByExam_ExamId(Long examId);
    /**
     * NEW - the per-student dedup check for the automatic reminder job.
     *
     * "Has THIS student already received a SUCCESSFUL reminder for THIS exam?"
     *
     * Spring Data builds the SQL from the method name:
     *   Exam_ExamId  -> join to exams via the 'exam' field, compare exam_id
     *   User_UserId  -> join to users via the 'user' field, compare user_id
     *   Status       -> compare the status column
     *
     * Only SENT rows count. FAILED rows do NOT block a retry - that is the
     * whole point: failed students get another attempt on the next run.
     */
    boolean existsByExam_ExamIdAndUser_UserIdAndStatus(
            Long examId, Long userId, EmailStatus status);
}
