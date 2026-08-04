package com.testforge.result.repository;

import com.testforge.result.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    /** All answers of one attempt -> used for the per-question review screen. */
    List<StudentAnswer> findByResult_ResultId(Long resultId);
}
