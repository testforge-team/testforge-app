package com.testforge.question.mapper;

import com.testforge.question.dto.QuestionAdminDto;
import com.testforge.question.dto.QuestionStudentDto;
import com.testforge.question.entity.Question;
import org.springframework.stereotype.Component;

/** One place that converts Question entities into the two response shapes. */
@Component
public class QuestionMapper {

    public QuestionAdminDto toAdminDto(Question q) {
        return QuestionAdminDto.builder()
                .questionId(q.getQuestionId())
                .questionText(q.getQuestionText())
                .optionA(q.getOptionA())
                .optionB(q.getOptionB())
                .optionC(q.getOptionC())
                .optionD(q.getOptionD())
                .correctOption(q.getCorrectOption())
                .topicId(q.getTopic().getTopicId())
                .topicName(q.getTopic().getTopicName())
                .build();
    }

    public QuestionStudentDto toStudentDto(Question q) {
        return QuestionStudentDto.builder()
                .questionId(q.getQuestionId())
                .questionText(q.getQuestionText())
                .optionA(q.getOptionA())
                .optionB(q.getOptionB())
                .optionC(q.getOptionC())
                .optionD(q.getOptionD())
                .topicName(q.getTopic().getTopicName())
                .build();      // note: correctOption never copied — it can't be, the field doesn't exist
    }
}
