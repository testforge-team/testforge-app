package com.testforge.result.service;

import com.testforge.exam.entity.Exam;
import com.testforge.exam.entity.ExamQuestion;
import com.testforge.exam.repository.ExamQuestionRepository;
import com.testforge.exam.repository.ExamRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE HEART OF THE APPLICATION.
 * Turns a raw submission into a graded, stored result. Follow the numbered
 * steps in gradeSubmission — this method IS the exam-submission sequence
 * diagram written in Java, and it is the #1 viva question for your area.
 */
@Service
@RequiredArgsConstructor
public class GradingService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ResultRepository resultRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;

    /**
     * @Transactional makes this whole method ALL-OR-NOTHING. It performs many
     * inserts (one Result + one StudentAnswer per question). If anything fails
     * halfway, the transaction rolls back and the database is left untouched —
     * you never get a half-saved result.
     *
     * @param studentEmail comes from the JWT (the controller passes Principal),
     *                     so a student can only ever submit AS THEMSELVES.
     */
    @Transactional
    public ResultDto gradeSubmission(String studentEmail, Long examId, SubmitExamRequest request) {

        // 1) WHO is submitting (from the trusted token, not the request body).
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // 2) WHICH exam.
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        // 3) The exam's official question set (from Person B's junction table).
        //    These are the ONLY questions that count; anything else is ignored.
        List<Question> examQuestions = examQuestionRepository.findByExam_ExamId(examId).stream()
                .map(ExamQuestion::getQuestion)
                .toList();

        int totalMarks = examQuestions.size();          // 1 mark per question
        if (totalMarks == 0) {
            throw new BadRequestException("This exam has no questions and cannot be graded.");
        }

        // 4) Turn the submitted list into a quick lookup: questionId -> selectedOption.
        //    We take the FIRST answer for any duplicated questionId (defensive).
        Map<Long, String> submitted = new HashMap<>();
        for (AnswerDto a : request.getAnswers()) {
            submitted.putIfAbsent(a.getQuestionId(), a.getSelectedOption());
        }

        // 5) Save the Result row FIRST (finalScore filled in a moment) so that
        //    each StudentAnswer can point back to it.
        Result result = resultRepository.save(Result.builder()
                .user(student)
                .exam(exam)
                .finalScore(0)
                .build());

        // 6) Grade every question of the EXAM (not just the ones submitted).
        //    A question the student never answered is saved as skipped (null,
        //    wrong). This guarantees exactly totalMarks answer rows every time.
        int score = 0;
        for (Question q : examQuestions) {
            String chosen = submitted.get(q.getQuestionId());          // null if skipped
            boolean correct = chosen != null && chosen.equalsIgnoreCase(q.getCorrectOption());
            if (correct) {
                score++;
            }
            studentAnswerRepository.save(StudentAnswer.builder()
                    .result(result)
                    .question(q)
                    .selectedOption(chosen)
                    .isCorrect(correct)
                    .build());
        }

        // 7) Store the final score on the Result.
        result.setFinalScore(score);
        resultRepository.save(result);

        // 8) Compute the derived numbers and the verdict.
        //    100.0 (a double!) avoids integer division giving 0.
        double percentage = (score * 100.0) / totalMarks;
        String status = (score >= exam.getPassingMarks()) ? "PASS" : "FAIL";

        return ResultDto.builder()
                .resultId(result.getResultId())
                .totalMarks(totalMarks)
                .obtainedMarks(score)
                .percentage(Math.round(percentage * 100.0) / 100.0)   // 2 decimals
                .status(status)
                .build();
    }
}
