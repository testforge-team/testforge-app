package com.testforge.exam.service;

import com.testforge.exam.dto.ExamAttemptDto;
import com.testforge.exam.dto.ExamDto;
import com.testforge.exam.dto.ExamRequest;
import com.testforge.exam.entity.Exam;
import com.testforge.exam.entity.ExamQuestion;
import com.testforge.exam.entity.ExamQuestionId;
import com.testforge.exam.repository.ExamQuestionRepository;
import com.testforge.exam.repository.ExamRepository;
import com.testforge.exception.BadRequestException;
import com.testforge.exception.ResourceNotFoundException;
import com.testforge.question.entity.Question;
import com.testforge.question.mapper.QuestionMapper;
import com.testforge.question.repository.QuestionRepository;
import com.testforge.user.entity.User;
import com.testforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;

    /**
     * Create an exam. 'adminEmail' comes from the JWT (see the controller):
     * we look the admin up and store them as created_by for accountability.
     */
    @Transactional
    public ExamDto createExam(ExamRequest req, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        Exam saved = examRepository.save(Exam.builder()
                .title(req.getTitle())
                .durationMinutes(req.getDurationMinutes())
                .passingMarks(req.getPassingMarks())
                .scheduledAt(req.getScheduledAt())
                .createdBy(admin)
                .build());
        return toDto(saved);
    }

    @Transactional
    public ExamDto updateExam(Long id, ExamRequest req) {
        Exam exam = findOr404(id);
        exam.setTitle(req.getTitle());
        exam.setDurationMinutes(req.getDurationMinutes());
        exam.setPassingMarks(req.getPassingMarks());
        exam.setScheduledAt(req.getScheduledAt());
        return toDto(examRepository.save(exam));
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = findOr404(id);
        // exam_questions rows vanish automatically (ON DELETE CASCADE in the SQL),
        // but results referencing the exam will block deletion — turn that
        // into a readable message.
        try {
            examRepository.delete(exam);
            examRepository.flush();
        } catch (Exception e) {
            throw new BadRequestException("Cannot delete: students have results for this exam.");
        }
    }

    /**
     * Map questions into an exam (fills the junction table).
     * We loop the ids; for each we verify the question exists, build the
     * composite key, skip it if already mapped, otherwise insert one
     * ExamQuestion row. Duplicates are skipped silently so the admin can
     * resend the same list without errors.
     */
    @Transactional
    public ExamDto addQuestionsToExam(Long examId, List<Long> questionIds) {
        Exam exam = findOr404(examId);

        for (Long qId : questionIds) {
            Question question = questionRepository.findById(qId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Question not found with id: " + qId));

            ExamQuestionId key = new ExamQuestionId(examId, qId);
            if (!examQuestionRepository.existsById(key)) {
                examQuestionRepository.save(ExamQuestion.builder()
                        .id(key)
                        .exam(exam)
                        .question(question)
                        .build());
            }
        }
        return toDto(exam);
    }

    /**
     * Exams a student may take. "Published" in our design simply means
     * the exam has at least one question mapped (the schema has no status
     * column by decision). Empty exams stay invisible to students.
     */
    @Transactional(readOnly = true)
    public List<ExamDto> getExamsForStudents() {
        return examRepository.findAll().stream()
                .map(this::toDto)
                .filter(dto -> dto.getTotalQuestions() > 0)
                .toList();
    }

    /** Admin list: sees every exam, including empty drafts. */
    @Transactional(readOnly = true)
    public List<ExamDto> getAllExamsForAdmin() {
        return examRepository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * THE ATTEMPT ENDPOINT'S DATA: exam settings + questions as
     * QuestionStudentDto — the class that physically has no correctOption
     * field, so the answer key cannot leak mid-exam.
     */
    @Transactional(readOnly = true)
    public ExamAttemptDto getExamForAttempt(Long examId) {
        Exam exam = findOr404(examId);

        List<Question> questions = examQuestionRepository.findByExam_ExamId(examId).stream()
                .map(ExamQuestion::getQuestion)
                .toList();

        if (questions.isEmpty()) {
            throw new BadRequestException("This exam has no questions yet.");
        }

        return ExamAttemptDto.builder()
                .examId(exam.getExamId())
                .title(exam.getTitle())
                .durationMinutes(exam.getDurationMinutes())
                .questions(questions.stream().map(questionMapper::toStudentDto).toList())
                .build();
    }

    private Exam findOr404(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
    }

    private ExamDto toDto(Exam e) {
        return ExamDto.builder()
                .examId(e.getExamId())
                .title(e.getTitle())
                .durationMinutes(e.getDurationMinutes())
                .passingMarks(e.getPassingMarks())
                .scheduledAt(e.getScheduledAt())
                .totalQuestions(examQuestionRepository.countByExam_ExamId(e.getExamId()))
                .build();
    }
}
