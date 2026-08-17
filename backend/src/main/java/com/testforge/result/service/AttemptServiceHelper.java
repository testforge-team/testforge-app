package com.testforge.result.service;

import com.testforge.exam.entity.ExamQuestion;
import com.testforge.exam.repository.ExamQuestionRepository;
import com.testforge.question.entity.Question;
import com.testforge.result.entity.Result;
import com.testforge.result.entity.StudentAnswer;
import com.testforge.result.repository.ResultRepository;
import com.testforge.result.repository.StudentAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * NEW FILE - a very small helper.
 *
 * WHY IT EXISTS: GradingService needs to save the last answers that arrive
 * with the submit call, and AttemptService needs GradingService to close an
 * expired attempt. If they called each other directly, Spring would report a
 * circular dependency at startup. Putting the shared save logic in this third
 * class breaks the circle.
 */
@Service
@RequiredArgsConstructor
public class AttemptServiceHelper {

    private final ResultRepository resultRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ExamQuestionRepository examQuestionRepository;

    /**
     * Saves one answer if it is valid. Anything invalid is ignored silently,
     * because this runs during submission and we must not fail the whole
     * submission over one bad entry.
     */
    @Transactional
    public void saveIfPossible(Long attemptId, Long questionId, String selectedOption) {
        if (questionId == null) return;

        Optional<Result> maybe = resultRepository.findById(attemptId);
        if (maybe.isEmpty()) return;
        Result attempt = maybe.get();

        Optional<Question> maybeQ = examQuestionRepository
                .findByExam_ExamId(attempt.getExam().getExamId()).stream()
                .map(ExamQuestion::getQuestion)
                .filter(q -> q.getQuestionId().equals(questionId))
                .findFirst();
        if (maybeQ.isEmpty()) return;      // not part of this exam - ignore
        Question question = maybeQ.get();

        boolean correct = selectedOption != null
                && selectedOption.equalsIgnoreCase(question.getCorrectOption());

        Optional<StudentAnswer> existing = studentAnswerRepository
                .findByResult_ResultIdAndQuestion_QuestionId(attemptId, questionId);

        if (existing.isPresent()) {
            StudentAnswer a = existing.get();
            a.setSelectedOption(selectedOption);
            a.setIsCorrect(correct);
            studentAnswerRepository.save(a);
        } else {
            studentAnswerRepository.save(StudentAnswer.builder()
                    .result(attempt)
                    .question(question)
                    .selectedOption(selectedOption)
                    .isCorrect(correct)
                    .build());
        }
    }
}
