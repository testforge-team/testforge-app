package com.testforge.topic.service;

import com.testforge.exception.BadRequestException;
import com.testforge.exception.ResourceNotFoundException;
import com.testforge.topic.dto.TopicDto;
import com.testforge.topic.entity.Topic;
import com.testforge.topic.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * All topic logic. Note the pattern used in every method:
 * validate -> act -> convert to DTO -> return.
 */
@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    public List<TopicDto> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public TopicDto createTopic(TopicDto request) {
        // Reject duplicates BEFORE hitting the DB constraint,
        // so the user gets a friendly message instead of a raw SQL error.
        if (topicRepository.existsByTopicName(request.getTopicName())) {
            throw new BadRequestException("Topic already exists: " + request.getTopicName());
        }
        Topic saved = topicRepository.save(
                Topic.builder().topicName(request.getTopicName()).build());
        return toDto(saved);
    }

    public TopicDto updateTopic(Long id, TopicDto request) {
        Topic topic = findOr404(id);
        // Renaming to a name that some OTHER topic already has -> reject.
        if (!topic.getTopicName().equals(request.getTopicName())
                && topicRepository.existsByTopicName(request.getTopicName())) {
            throw new BadRequestException("Topic already exists: " + request.getTopicName());
        }
        topic.setTopicName(request.getTopicName());
        return toDto(topicRepository.save(topic));
    }

    public void deleteTopic(Long id) {
        Topic topic = findOr404(id);
        try {
            topicRepository.delete(topic);
            topicRepository.flush();   // force the SQL now so FK errors surface here
        } catch (Exception e) {
            // The DB blocks deleting a topic that questions still reference
            // (foreign key). Turn that raw error into a helpful message.
            throw new BadRequestException(
                "Cannot delete: questions still use this topic. Reassign them first.");
        }
    }

    /** The load-or-404 helper — the same one-liner every module uses. */
    private Topic findOr404(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));
    }

    private TopicDto toDto(Topic t) {
        return TopicDto.builder().topicId(t.getTopicId()).topicName(t.getTopicName()).build();
    }
}
