package com.testforge.analytics.entity;

import com.testforge.common.enums.WeaknessStatus;
import com.testforge.topic.entity.Topic;
import com.testforge.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row of 'student_weaknesses' = one student's proficiency in one topic.
 * The database has UNIQUE(user_id, topic_id), so there is at most ONE row
 * per (student, topic) pair — we UPDATE it over time rather than inserting
 * duplicates. This is the "upsert" pattern (update-or-insert).
 */
@Entity
@Table(name = "student_weaknesses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentWeakness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weakness_id")
    private Long weaknessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WeaknessStatus status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
