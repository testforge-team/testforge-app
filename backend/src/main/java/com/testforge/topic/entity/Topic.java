package com.testforge.topic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row of the 'topics' table: a subject category like "SQL" or "Collections".
 * Every question belongs to exactly one topic, and Person D's analytics
 * group results by topic — so this small table powers a lot of the system.
 */
@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // PostgreSQL assigns the next id
    @Column(name = "topic_id")
    private Long topicId;

    // unique = true mirrors the UNIQUE constraint in the database:
    // two topics can never share a name, so analytics never split
    // between "SQL" and a duplicate "SQL".
    @Column(name = "topic_name", nullable = false, unique = true, length = 100)
    private String topicName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Hibernate calls this automatically just before the first INSERT. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
