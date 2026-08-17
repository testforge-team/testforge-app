package com.testforge.result.service;

import com.testforge.common.enums.AttemptStatus;
import com.testforge.common.enums.ExamStatus;
import com.testforge.exam.entity.Exam;
import com.testforge.exam.entity.ExamQuestion;
import com.testforge.exam.repository.ExamQuestionRepository;
import com.testforge.exam.repository.ExamRepository;
import com.testforge.exception.BadRequestException;
import com.testforge.exception.ResourceNotFoundException;
import com.testforge.question.dto.QuestionStudentDto;
import com.testforge.question.entity.Question;
import com.testforge.question.mapper.QuestionMapper;
import com.testforge.result.dto.AttemptStateDto;
import com.testforge.result.dto.ResultDto;
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
import java.util.Optional;

/**
 * NEW FILE - the service that makes an attempt survive a page refresh.
 *
 * The idea in one line: the attempt lives on the SERVER, not in the browser.
 * The browser only asks "what is my state?" and "please save this answer".
 *
 * Three public methods:
 *   startOrResume(...)  called when the exam screen opens (first time OR refresh)
 *   saveAnswer(...)     called every time the student clicks an option
 *   isOwnedBy(...)      a small safety check used by the controller
 */
@Service
@RequiredArgsConstructor
public class AttemptService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ResultRepository resultRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;
    private final GradingService gradingService;

    // ============================================================
    // 1. START A NEW ATTEMPT, OR RESUME THE ONE ALREADY RUNNING
    // ============================================================
    /**
     * This single method handles every way the exam screen can open:
     *
     *   a) first time          -> create a new attempt, full time
     *   b) after a refresh     -> resume, with the REMAINING time and the
     *                             answers already saved
     *   c) came back too late  -> the server grades what was saved and tells
     *                             the browser to go to the result page
     *
     * Because the browser cannot tell these apart, it just calls this and
     * uses whatever comes back. That keeps the React code simple.
     */
    @Transactional
    public AttemptStateDto startOrResume(String studentEmail, Long examId) {

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        // ---- is there an unfinished attempt for this student and exam? ----
        Optional<Result> existing = resultRepository
                .findByUser_UserIdAndExam_ExamIdAndStatus(
                        student.getUserId(), examId, AttemptStatus.IN_PROGRESS);

        if (existing.isPresent()) {
            Result attempt = existing.get();

            // CASE (c): the deadline passed while the student was away.
            // Nobody was there to submit, so the server does it now, using
            // whatever answers were saved before they left.
            if (attempt.isTimeOver()) {
                ResultDto graded = gradingService.finalizeAttempt(attempt, AttemptStatus.EXPIRED);
                return AttemptStateDto.builder()
                        .expired(true)
                        .resultId(graded.getResultId())
                        .examId(examId)
                        .title(exam.getTitle())
                        .message("Your time for this exam is over. It has been submitted automatically.")
                        .build();
            }

            // CASE (b): still within time -> RESUME.
            return buildState(exam, attempt, true);
        }

        // ---- no unfinished attempt, so this is a fresh start ----

        // The exam itself must be open right now.
        if (exam.getStatus() == ExamStatus.NOT_STARTED) {
            throw new BadRequestException(
                    "This exam has not started yet. It opens at " + exam.getScheduledAt());
        }
        if (exam.getStatus() == ExamStatus.EXPIRED) {
            throw new BadRequestException("This exam is closed. It ended at " + exam.getEndsAt());
        }

        long questionCount = examQuestionRepository.countByExam_ExamId(examId);
        if (questionCount == 0) {
            throw new BadRequestException("This exam has no questions yet.");
        }

        // CASE (a): create the attempt row.
        LocalDateTime now = LocalDateTime.now();

        // The personal deadline is "now + duration", BUT it can never go past
        // the exam's own closing time. So a student starting 10 minutes before
        // the exam closes gets 10 minutes, not the full duration.
        LocalDateTime personalEnd = now.plusMinutes(exam.getDurationMinutes());
        LocalDateTime examEnd = exam.getEndsAt();
        LocalDateTime endTime = personalEnd.isBefore(examEnd) ? personalEnd : examEnd;

        Result attempt = resultRepository.save(Result.builder()
                .user(student)
                .exam(exam)
                .finalScore(0)
                .examDate(now)
                .startedAt(now)
                .endTime(endTime)
                .status(AttemptStatus.IN_PROGRESS)
                .build());

        return buildState(exam, attempt, false);
    }

    // ============================================================
    // 2. SAVE ONE ANSWER (called on every click)
    // ============================================================
    /**
     * Stores or updates the student's choice for one question.
     *
     * We look for an existing row for this (attempt, question) pair. If it is
     * there we UPDATE it, because the student changed their mind; otherwise we
     * INSERT. That is why the database has a unique index on that pair.
     *
     * is_correct is filled in here as well, so grading at the end is only a
     * matter of counting - and nothing is lost if the student disappears.
     */
    @Transactional
    public void saveAnswer(Long attemptId, Long questionId, String selectedOption) {

        Result attempt = resultRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        // A finished attempt must never accept new answers.
        if (!attempt.isInProgress()) {
            throw new BadRequestException("This attempt is already submitted.");
        }
        // Neither may one whose time is over - this blocks a browser that kept
        // sending answers after the deadline.
        if (attempt.isTimeOver()) {
            throw new BadRequestException("Time is over for this attempt.");
        }

        // The question must actually belong to this exam.
        Question question = examQuestionRepository
                .findByExam_ExamId(attempt.getExam().getExamId()).stream()
                .map(ExamQuestion::getQuestion)
                .filter(q -> q.getQuestionId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "This question does not belong to the exam."));

        boolean correct = selectedOption != null
                && selectedOption.equalsIgnoreCase(question.getCorrectOption());

        Optional<StudentAnswer> existing = studentAnswerRepository
                .findByResult_ResultIdAndQuestion_QuestionId(attemptId, questionId);

        if (existing.isPresent()) {
            StudentAnswer answer = existing.get();          // changed their mind
            answer.setSelectedOption(selectedOption);
            answer.setIsCorrect(correct);
            studentAnswerRepository.save(answer);
        } else {
            studentAnswerRepository.save(StudentAnswer.builder()   // first time
                    .result(attempt)
                    .question(question)
                    .selectedOption(selectedOption)
                    .isCorrect(correct)
                    .build());
        }
    }

    // ============================================================
    // 3. SMALL HELPERS
    // ============================================================

    /** Used by the controller so one student cannot save into another's attempt. */
    @Transactional(readOnly = true)
    public boolean isOwnedBy(Long attemptId, String email) {
        return resultRepository.findById(attemptId)
                .map(r -> r.getUser().getEmail().equalsIgnoreCase(email))
                .orElse(false);
    }

    /**
     * Builds the payload the exam screen needs: the questions WITHOUT the
     * answer key, the answers already saved, and the seconds left.
     */
    private AttemptStateDto buildState(Exam exam, Result attempt, boolean resumed) {

        List<Question> questions = examQuestionRepository
                .findByExam_ExamId(exam.getExamId()).stream()
                .map(ExamQuestion::getQuestion)
                .toList();

        // Turn the saved rows into a simple map the browser can use directly.
        Map<Long, String> saved = new HashMap<>();
        for (StudentAnswer a : studentAnswerRepository.findByResult_ResultId(attempt.getResultId())) {
            if (a.getSelectedOption() != null) {
                saved.put(a.getQuestion().getQuestionId(), a.getSelectedOption());
            }
        }

        return AttemptStateDto.builder()
                .attemptId(attempt.getResultId())
                .examId(exam.getExamId())
                .title(exam.getTitle())
                .durationMinutes(exam.getDurationMinutes())
                // THE IMPORTANT LINE: the clock comes from the server's stored
                // end time, so a refresh cannot reset or extend it.
                .remainingSeconds(attempt.getRemainingSeconds())
                .resumed(resumed)
                .questions(questions.stream().map(questionMapper::toStudentDto).toList())
                .savedAnswers(saved)
                .expired(false)
                .build();
    }
}
