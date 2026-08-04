package com.testforge.question.service;

import com.testforge.exception.BadRequestException;
import com.testforge.exception.ResourceNotFoundException;
import com.testforge.question.dto.QuestionAdminDto;
import com.testforge.question.dto.QuestionRequest;
import com.testforge.question.entity.Question;
import com.testforge.question.mapper.QuestionMapper;
import com.testforge.question.repository.QuestionRepository;
import com.testforge.topic.entity.Topic;
import com.testforge.topic.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final QuestionMapper questionMapper;

    /**
     * @Transactional(readOnly = true) keeps the DB session open while we map,
     * so the LAZY topic relation can still be loaded inside toAdminDto().
     * Without it you'd meet the famous LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public List<QuestionAdminDto> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(questionMapper::toAdminDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionAdminDto> getQuestionsByTopic(Long topicId) {
        return questionRepository.findByTopic_TopicId(topicId).stream()
                .map(questionMapper::toAdminDto)
                .toList();
    }

    @Transactional
    public QuestionAdminDto createQuestion(QuestionRequest req) {
        Topic topic = topicRepository.findById(req.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Topic not found with id: " + req.getTopicId()));

        Question saved = questionRepository.save(Question.builder()
                .topic(topic)
                .questionText(req.getQuestionText())
                .optionA(req.getOptionA())
                .optionB(req.getOptionB())
                .optionC(req.getOptionC())
                .optionD(req.getOptionD())
                .correctOption(req.getCorrectOption())
                .build());
        return questionMapper.toAdminDto(saved);
    }

    @Transactional
    public QuestionAdminDto updateQuestion(Long id, QuestionRequest req) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        Topic topic = topicRepository.findById(req.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Topic not found with id: " + req.getTopicId()));

        q.setTopic(topic);
        q.setQuestionText(req.getQuestionText());
        q.setOptionA(req.getOptionA());
        q.setOptionB(req.getOptionB());
        q.setOptionC(req.getOptionC());
        q.setOptionD(req.getOptionD());
        q.setCorrectOption(req.getCorrectOption());
        return questionMapper.toAdminDto(questionRepository.save(q));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        try {
            questionRepository.delete(q);
            questionRepository.flush();
        } catch (Exception e) {
            // exam_questions or student_answers may reference it
            throw new BadRequestException(
                "Cannot delete: this question is used in an exam or has recorded answers.");
        }
    }
}
