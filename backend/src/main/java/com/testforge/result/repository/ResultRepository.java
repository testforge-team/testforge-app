package com.testforge.result.repository;

import com.testforge.result.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    /**
     * A student's attempt history, newest first.
     * The method name encodes: WHERE user_id = ? ORDER BY exam_date DESC.
     */
    List<Result> findByUser_UserIdOrderByExamDateDesc(Long userId);
}
