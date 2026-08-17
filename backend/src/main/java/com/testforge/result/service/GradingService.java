package com.testforge.result.service;

import com.testforge.analytics.service.WeaknessService;
import com.testforge.common.enums.AttemptStatus;
import com.testforge.exam.entity.Exam;
import com.testforge.exam.entity.ExamQuestion;
import com.testforge.exam.repository.ExamQuestionRepository;
import com.testforge.exception.BadRequestException;
import com.testforge.exception.ResourceNotFoundException;
import com.testforge.question.entity.Question;
import com.testforge.result.dto.AnswerDto;
import com.testforge.result.dto.ResultDto;
import com.testforge.result.dto.SubmitExamRequest;
import com.testforge.result.entity.Result;
import com.testforge.result.entity.StudentAnswer;
import com.testforge.result.repository.ResultRepository;
import com.testforge.result.repository.StudentAnswerRepository;
import com.testforge.user.entity.User;
import com.testforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE HEART OF THE APPLICATION - now working on a SAVED attempt.
 *
 * WHAT CHANGED IN THIS VERSION
 * Before, this method created the result row and graded everything in one go,
 * using the answers that arrived in the request. Now the attempt row already
 * exists (AttemptService created it at Start) and most answers are already
 * saved, so grading has two steps:
 *
 *   1. store any last answers that came with the submit call (a safety net
 *      in case the final click did not reach the server), then
 *   2. finalizeAttempt() - count, compute and close the attempt.
 *
 * finalizeAttempt() is public because THREE different callers use it:
 *   - the student pressing Submit          -> SUBMITTED
 *   - the browser timer reaching zero      -> AUTO_SUBMITTED
 *   - the server, for an abandoned attempt -> EXPIRED
 * All three grade identically, which is exactly what we want.
 */
@Service
@RequiredArgsConstructor
public class GradingService {

    private final ExamQuestionRepository examQuestionRepository;
    private final ResultRepository resultRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;
    private final WeaknessService weaknessService;
    private final AttemptServiceHelper helper;   // tiny helper, defined below

    /**
     * Called by the submit endpoint.
     *
     * @param autoSubmitted true when the browser's countdown hit zero,
     *                      false when the student pressed the button.
     */
    @Transactional
    public ResultDto submitAttempt(String studentEmail, Long examId,
                                   SubmitExamRequest request, boolean autoSubmitted) {

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Find the attempt that is currently running for this student and exam.
        Result attempt = resultRepository.findByUser_UserIdAndExam_ExamIdAndStatus(
                        student.getUserId(), examId, AttemptStatus.IN_PROGRESS)
                .orElseThrow(() -> new BadRequestException(
                        "No attempt in progress for this exam. It may already be submitted."));

        // ---- 1. store any answers that came with the submit call ----
        // Normally these are already saved by the auto-save, but the very last
        // click might not have reached the server yet. Saving them again is
        // harmless because saveAnswer updates instead of duplicating.
        if (request != null && request.getAnswers() != null) {
            for (AnswerDto a : request.getAnswers()) {
                helper.saveIfPossible(attempt.getResultId(), a.getQuestionId(), a.getSelectedOption());
            }
        }

        // ---- 2. close and grade it ----
        AttemptStatus finalStatus = autoSubmitted
                ? AttemptStatus.AUTO_SUBMITTED
                : AttemptStatus.SUBMITTED;

        return finalizeAttempt(attempt, finalStatus);
    }

    /**
     * Grades a saved attempt and closes it. This is the single place where a
     * score is produced, no matter how the attempt ended.
     */
    @Transactional
    public ResultDto finalizeAttempt(Result attempt, AttemptStatus finalStatus) {

        Exam exam = attempt.getExam();

        // The exam's OFFICIAL questions. Grading only ever considers these,
        // which is why a stray question id can never affect the score.
        List<Question> examQuestions = examQuestionRepository
                .findByExam_ExamId(exam.getExamId()).stream()
                .map(ExamQuestion::getQuestion)
                .toList();

        int totalMarks = examQuestions.size();          // one mark per question
        if (totalMarks == 0) {
            throw new BadRequestException("This exam has no questions and cannot be graded.");
        }

        // What has been saved so far, as a quick lookup.
        Map<Long, StudentAnswer> saved = new HashMap<>();
        for (StudentAnswer a : studentAnswerRepository.findByResult_ResultId(attempt.getResultId())) {
            saved.put(a.getQuestion().getQuestionId(), a);
        }

        // Walk the EXAM's questions, not the saved ones. This guarantees one
        // answer row per question: a question the student never touched is
        // written now as skipped (null option, not correct).
        int score = 0;
        for (Question q : examQuestions) {
            StudentAnswer answer = saved.get(q.getQuestionId());

            if (answer == null) {
                studentAnswerRepository.save(StudentAnswer.builder()
                        .result(attempt)
                        .question(q)
                        .selectedOption(null)      // skipped
                        .isCorrect(false)
                        .build());
                continue;
            }

            // Re-check correctness here rather than trusting the stored flag,
            // so an edited question cannot leave a stale value behind.
            boolean correct = answer.getSelectedOption() != null
                    && answer.getSelectedOption().equalsIgnoreCase(q.getCorrectOption());
            if (correct != Boolean.TRUE.equals(answer.getIsCorrect())) {
                answer.setIsCorrect(correct);
                studentAnswerRepository.save(answer);
            }
            if (correct) score++;
        }

        // ---- close the attempt ----
        attempt.setFinalScore(score);
        attempt.setStatus(finalStatus);
        attempt.setExamDate(LocalDateTime.now());   // the real submission time
        resultRepository.save(attempt);

        // ---- Person D's weakness analysis, inside this same transaction ----
        weaknessService.analyzeAfterSubmission(attempt.getResultId());

        // ---- the numbers the student sees ----
        // 100.0 is a DOUBLE on purpose: writing 100 would make Java do integer
        // division and 3 out of 5 would come out as 0.
        double percentage = (score * 100.0) / totalMarks;
        String passFail = (score >= exam.getPassingMarks()) ? "PASS" : "FAIL";

        return ResultDto.builder()
                .resultId(attempt.getResultId())
                .totalMarks(totalMarks)
                .obtainedMarks(score)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .status(passFail)
                .build();
    }
}
