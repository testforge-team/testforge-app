package com.testforge.topic.repository;

import com.testforge.topic.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Database access for topics. Extending JpaRepository gives us
 * save(), findById(), findAll(), deleteById() etc. for free.
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    // Spring turns this method NAME into SQL:
    // SELECT EXISTS(SELECT 1 FROM topics WHERE topic_name = ?)
    boolean existsByTopicName(String topicName);
}
