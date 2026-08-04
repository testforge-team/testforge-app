package com.testforge.analytics.service;

import com.testforge.analytics.entity.StudentWeakness;
import com.testforge.analytics.repository.StudentWeaknessRepository;
import com.testforge.common.enums.WeaknessStatus;
import com.testforge.result.entity.Result;
import com.testforge.result.entity.StudentAnswer;
import com.testforge.result.repository.ResultRepository;
import com.testforge.result.repository.StudentAnswerRepository;
import com.testforge.topic.entity.Topic;
import com.testforge.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Updates a student's per-topic proficiency after they finish an exam.
 *
 * THIS IS THE INTERLINK WITH PERSON C: their GradingService calls
 * analyzeAfterSubmission(resultId) as its last step, inside the same
 * transaction, so grading + weakness update succeed or fail together.
 */
@Service
@RequiredArgsConstructor
public class WeaknessService {

    private final ResultRepository resultRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final StudentWeaknessRepository weaknessRepository;

    // Small helper class to tally correct/total per topic while we loop.
    private static class Tally { int correct; int total; }

    @Transactional
    public void analyzeAfterSubmission(Long resultId) {
        Result result = resultRepository.findById(resultId).orElse(null);
        if (result == null) return;              // nothing to do; never crash the caller
        User student = result.getUser();

        // 1) Gather this attempt's answers and tally them by topic.
        List<StudentAnswer> answers = studentAnswerRepository.findByResult_ResultId(resultId);
        Map<Topic, Tally> byTopic = new HashMap<>();
        for (StudentAnswer a : answers) {
            Topic topic = a.getQuestion().getTopic();
            Tally t = byTopic.computeIfAbsent(topic, k -> new Tally());
            t.total++;
            if (Boolean.TRUE.equals(a.getIsCorrect())) t.correct++;
        }

        // 2) For each topic, decide a status and UPSERT the weakness row.
        for (Map.Entry<Topic, Tally> e : byTopic.entrySet()) {
            Topic topic = e.getKey();
            Tally t = e.getValue();
            double accuracy = (t.total == 0) ? 0 : (t.correct * 100.0 / t.total);

            // Only track the extremes; middling topics are left as-is.
            WeaknessStatus status;
            if (accuracy < 50) {
                status = WeaknessStatus.NEEDS_REVISION;
            } else if (accuracy >= 80) {
                status = WeaknessStatus.MASTERED;
            } else {
                continue;   // 50-79%: not weak, not mastered -> skip
            }

            upsert(student, topic, status);
        }
    }

    /**
     * "Upsert" = update the existing (student, topic) row, or insert a new one.
     * The UNIQUE(user_id, topic_id) constraint guarantees a single row per pair.
     */
    private void upsert(User student, Topic topic, WeaknessStatus status) {
        StudentWeakness row = weaknessRepository
                .findByUser_UserIdAndTopic_TopicId(student.getUserId(), topic.getTopicId())
                .orElse(StudentWeakness.builder().user(student).topic(topic).build());

        row.setStatus(status);
        row.setUpdatedAt(LocalDateTime.now());
        weaknessRepository.save(row);
    }
}
