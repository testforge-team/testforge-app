package com.testforge.exam.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.testforge.exam.entity.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    // findAll(), findById(), save(), deleteById() are inherited and enough here.
    // Person D can later add: List<Exam> findByScheduledAtBetween(start, end)
    // for the 24-hour reminder job.
	List<Exam> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
	
}
