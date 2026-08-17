package com.testforge.exam.service;

import com.testforge.common.enums.ExamStatus;
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

    /** Default window if the admin does not choose one. */
    private static final int DEFAULT_ACTIVE_HOURS = 24;

    @Transactional
    public ExamDto createExam(ExamRequest req, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        Exam saved = examRepository.save(Exam.builder()
                .title(req.getTitle())
                .durationMinutes(req.getDurationMinutes())
                .passingMarks(req.getPassingMarks())
                .scheduledAt(req.getScheduledAt())
                // NEW: use the given window, or fall back to 24 hours
                .activeHours(req.getActiveHours() == null
                        ? DEFAULT_ACTIVE_HOURS : req.getActiveHours())
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
        if (req.getActiveHours() != null) {
            exam.setActiveHours(req.getActiveHours());
        }
        return toDto(examRepository.save(exam));
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = findOr404(id);
        try {
            examRepository.delete(exam);
            examRepository.flush();
        } catch (Exception e) {
            throw new BadRequestException("Cannot delete: students have results for this exam.");
        }
    }

    @Transactional
    public ExamDto addQuestionsToExam(Long examId, List<Long> questionIds) {
        Exam exam = findOr404(examId);
        for (Long qId : questionIds) {
            Question question = questionRepository.findById(qId)
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + qId));
            ExamQuestionId key = new ExamQuestionId(examId, qId);
            if (!examQuestionRepository.existsById(key)) {
                examQuestionRepository.save(ExamQuestion.builder()
                        .id(key).exam(exam).question(question).build());
            }
        }
        return toDto(exam);
    }

    /**
     * Exams a student may see.
     *
     * WHAT CHANGED: we still hide exams with no questions (that is our
     * publishing rule), but we now also hide ones whose window has CLOSED,
     * because a student can no longer do anything with them. Upcoming exams
     * are kept, so the student can see what is coming - the dashboard shows
     * them with a "Starts at ..." badge and no Start button.
     */
    @Transactional(readOnly = true)
    public List<ExamDto> getExamsForStudents() {
        return examRepository.findAll().stream()
                .map(this::toDto)
                .filter(dto -> dto.getTotalQuestions() > 0)
                .filter(dto -> !ExamStatus.EXPIRED.name().equals(dto.getStatus()))
                .toList();
    }

    /** Admin list: every exam, including drafts and closed ones. */
    @Transactional(readOnly = true)
    public List<ExamDto> getAllExamsForAdmin() {
        return examRepository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * The questions for an attempt, WITHOUT the correct answers.
     *
     * NOTE: the exam screen now normally calls POST /exams/{id}/start instead,
     * which also returns the timer and saved answers. This method is kept
     * because it is still useful for a preview and does no harm.
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

    /**
     * Entity to DTO. The three new values (activeHours, endsAt, status) are
     * calculated here every time, so they can never be stale.
     */
    private ExamDto toDto(Exam e) {
        return ExamDto.builder()
                .examId(e.getExamId())
                .title(e.getTitle())
                .durationMinutes(e.getDurationMinutes())
                .passingMarks(e.getPassingMarks())
                .scheduledAt(e.getScheduledAt())
                .totalQuestions(examQuestionRepository.countByExam_ExamId(e.getExamId()))
                .activeHours(e.getActiveHours())
                .endsAt(e.getEndsAt())
                .status(e.getStatus().name())
                .build();
    }
}
