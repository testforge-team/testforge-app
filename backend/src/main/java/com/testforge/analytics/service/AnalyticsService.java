package com.testforge.analytics.service;

import com.testforge.analytics.dto.*;
import com.testforge.analytics.entity.StudentWeakness;
import com.testforge.analytics.repository.StudentWeaknessRepository;
import com.testforge.result.entity.Result;
import com.testforge.result.entity.StudentAnswer;
import com.testforge.result.repository.ResultRepository;
import com.testforge.result.repository.StudentAnswerRepository;
import com.testforge.topic.entity.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only analytics: a student's performance view, and the admin's
 * whole-batch difficult-topics report. Both are computed in plain Java
 * loops from Person C's data — simple and perfectly fast at project scale.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ResultRepository resultRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final StudentWeaknessRepository weaknessRepository;

    private static class Tally { int correct; int total; }

    /** Everything the student's Performance Analysis screen needs. */
    @Transactional(readOnly = true)
    public StudentPerformanceDto getStudentPerformance(Long studentId) {
        List<Result> results = resultRepository.findByUser_UserIdOrderByExamDateDesc(studentId);

        // --- Trend line: percentage per past attempt ---
        List<TrendPointDto> trends = new ArrayList<>();
        Map<Topic, Tally> topicTally = new HashMap<>();

        for (Result r : results) {
            List<StudentAnswer> answers = studentAnswerRepository.findByResult_ResultId(r.getResultId());
            int total = answers.size();
            double pct = (total == 0) ? 0 : Math.round((r.getFinalScore() * 100.0 / total) * 100.0) / 100.0;
            trends.add(TrendPointDto.builder()
                    .examTitle(r.getExam().getTitle())
                    .percentage(pct)
                    .examDate(r.getExamDate())
                    .build());

            // accumulate per-topic accuracy across ALL attempts
            for (StudentAnswer a : answers) {
                Topic topic = a.getQuestion().getTopic();
                Tally t = topicTally.computeIfAbsent(topic, k -> new Tally());
                t.total++;
                if (Boolean.TRUE.equals(a.getIsCorrect())) t.correct++;
            }
        }

        // --- Split topics into strengths (>=80%) and weaknesses (<50%) ---
        List<WeakTopicDto> strengths = new ArrayList<>();
        List<WeakTopicDto> weakTopics = new ArrayList<>();
        for (Map.Entry<Topic, Tally> e : topicTally.entrySet()) {
            Tally t = e.getValue();
            double acc = (t.total == 0) ? 0 : Math.round((t.correct * 100.0 / t.total) * 100.0) / 100.0;
            if (acc >= 80) {
                strengths.add(WeakTopicDto.builder()
                        .topicName(e.getKey().getTopicName()).accuracy(acc).status("MASTERED").build());
            } else if (acc < 50) {
                weakTopics.add(WeakTopicDto.builder()
                        .topicName(e.getKey().getTopicName()).accuracy(acc).status("NEEDS_REVISION").build());
            }
        }

        return StudentPerformanceDto.builder()
                .trends(trends).strengths(strengths).weakTopics(weakTopics).build();
    }

    /** Admin report: topics ranked by how often the WHOLE batch gets them wrong. */
    @Transactional(readOnly = true)
    public List<DifficultTopicDto> getDifficultTopics() {
        Map<Topic, Tally> tally = new HashMap<>();
        for (StudentAnswer a : studentAnswerRepository.findAll()) {
            Topic topic = a.getQuestion().getTopic();
            Tally t = tally.computeIfAbsent(topic, k -> new Tally());
            t.total++;
            if (Boolean.TRUE.equals(a.getIsCorrect())) t.correct++;
        }

        List<DifficultTopicDto> report = new ArrayList<>();
        for (Map.Entry<Topic, Tally> e : tally.entrySet()) {
            Tally t = e.getValue();
            double failureRate = (t.total == 0) ? 0
                    : Math.round(((t.total - t.correct) * 100.0 / t.total) * 100.0) / 100.0;
            report.add(DifficultTopicDto.builder()
                    .topicName(e.getKey().getTopicName())
                    .totalAnswers(t.total)
                    .failureRate(failureRate)
                    .build());
        }
        // Hardest topics first.
        report.sort((a, b) -> Double.compare(b.getFailureRate(), a.getFailureRate()));
        return report;
    }
}
