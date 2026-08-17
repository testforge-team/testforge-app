package com.testforge.result.service;

import com.testforge.exception.ResourceNotFoundException;
import com.testforge.question.entity.Question;
import com.testforge.result.dto.*;
import com.testforge.result.entity.Result;
import com.testforge.result.entity.StudentAnswer;
import com.testforge.result.repository.ResultRepository;
import com.testforge.result.repository.StudentAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//v2
import com.testforge.common.enums.AttemptStatus;

/**
 * Read-only views built from data GradingService produced:
 * a student's history list, and the full detail of one attempt.
 */
@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    /** A student's past attempts, newest first. */
    @Transactional(readOnly = true)
    public List<ResultHistoryDto> getHistory(Long studentId) {
        List<Result> results = resultRepository.findByUser_UserIdOrderByExamDateDesc(studentId);
        List<ResultHistoryDto> out = new ArrayList<>();

        for (Result r : results) {
            // NEW: skip attempts that are still being written
            if (r.getStatus() == AttemptStatus.IN_PROGRESS) {
                continue;
            }

            List<StudentAnswer> answers = studentAnswerRepository.findByResult_ResultId(r.getResultId());
            int total = answers.size();
            int obtained = r.getFinalScore();
            out.add(ResultHistoryDto.builder()
                    .resultId(r.getResultId())
                    .examTitle(r.getExam().getTitle())
                    .obtainedMarks(obtained)
                    .totalMarks(total)
                    .percentage(pct(obtained, total))
                    .status(obtained >= r.getExam().getPassingMarks() ? "PASS" : "FAIL")
                    .examDate(r.getExamDate())
                    .build());
        }
        return out;
    }

    /**
     * Full detail of one attempt: headline + per-question review + topic breakdown.
     */
    @Transactional(readOnly = true)
    public ResultDetailDto getDetail(Long resultId) {
        Result r = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id: " + resultId));

        List<StudentAnswer> answers = studentAnswerRepository.findByResult_ResultId(resultId);

        // Build the per-question review list.
        List<AnswerReviewDto> review = new ArrayList<>();
        // Build the topic breakdown as we go: topicName -> [correct, total].
        Map<String, int[]> byTopic = new LinkedHashMap<>();

        for (StudentAnswer a : answers) {
            Question q = a.getQuestion();
            String topicName = q.getTopic().getTopicName();

            review.add(AnswerReviewDto.builder()
                    .questionId(q.getQuestionId())
                    .questionText(q.getQuestionText())
                    .topicName(topicName)
                    .selectedOption(a.getSelectedOption())
                    .correctOption(q.getCorrectOption()) // safe now: exam is over
                    .correct(Boolean.TRUE.equals(a.getIsCorrect()))
                    .build());

            int[] tally = byTopic.computeIfAbsent(topicName, k -> new int[2]);
            tally[1]++; // total for this topic
            if (Boolean.TRUE.equals(a.getIsCorrect())) {
                tally[0]++; // correct for this topic
            }
        }

        List<TopicBreakdownDto> breakdown = new ArrayList<>();
        for (Map.Entry<String, int[]> e : byTopic.entrySet()) {
            int correct = e.getValue()[0];
            int total = e.getValue()[1];
            breakdown.add(TopicBreakdownDto.builder()
                    .topicName(e.getKey())
                    .correct(correct)
                    .total(total)
                    .accuracy(pct(correct, total))
                    .build());
        }

        int obtained = r.getFinalScore();
        int total = answers.size();
        return ResultDetailDto.builder()
                .resultId(r.getResultId())
                .examTitle(r.getExam().getTitle())
                .obtainedMarks(obtained)
                .totalMarks(total)
                .percentage(pct(obtained, total))
                .status(obtained >= r.getExam().getPassingMarks() ? "PASS" : "FAIL")
                .answers(review)
                .topicBreakdown(breakdown)
                .build();
    }

    /** Safe percentage with 2 decimals; guards against divide-by-zero. */
    private double pct(int part, int whole) {
        if (whole == 0)
            return 0.0;
        return Math.round((part * 100.0 / whole) * 100.0) / 100.0;
    }
}
