package com.testforge.analytics.repository;

import com.testforge.analytics.entity.StudentWeakness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentWeaknessRepository extends JpaRepository<StudentWeakness, Long> {

    /** Used by the upsert: is there already a row for this student+topic? */
    Optional<StudentWeakness> findByUser_UserIdAndTopic_TopicId(Long userId, Long topicId);

    /** All of a student's weakness/mastery rows, for their dashboard. */
    List<StudentWeakness> findByUser_UserId(Long userId);
}
