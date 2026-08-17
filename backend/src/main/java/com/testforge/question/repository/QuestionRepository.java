package com.testforge.question.repository;

import com.testforge.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * The underscore navigates INTO the relation:
     * Topic_TopicId = question.topic.topicId.
     * Generated SQL: SELECT * FROM questions WHERE topic_id = ?
     */
    List<Question> findByTopic_TopicId(Long topicId);
}
