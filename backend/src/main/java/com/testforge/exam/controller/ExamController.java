package com.testforge.exam.controller;

import com.testforge.exam.dto.ExamAttemptDto;
import com.testforge.exam.dto.ExamDto;
import com.testforge.exam.dto.ExamRequest;
import com.testforge.exam.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    /**
     * Student-facing list of takeable exams.
     * Principal is Spring's tiny interface for "who is logged in?" —
     * the JWT filter set it, and principal.getName() returns the email.
     * Here we only need to know they're authenticated, so we don't use it.
     */
    @GetMapping
    public ResponseEntity<List<ExamDto>> getExams() {
        return ResponseEntity.ok(examService.getExamsForStudents());
    }

    /** Admin list including empty (draft) exams. */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExamDto>> getAllExamsForAdmin() {
        return ResponseEntity.ok(examService.getAllExamsForAdmin());
    }

    /** The student's exam screen calls this when the attempt starts. */
    @GetMapping("/{id}/attempt")
    public ResponseEntity<ExamAttemptDto> getExamForAttempt(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamForAttempt(id));
    }

    /**
     * Create an exam. We pass principal.getName() (the admin's email from
     * the JWT) into the service so created_by is recorded — the client
     * cannot fake it because it comes from the signed token, not the body.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamDto> createExam(@Valid @RequestBody ExamRequest req,
                                              Principal principal) {
        return ResponseEntity.ok(examService.createExam(req, principal.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamDto> updateExam(@PathVariable Long id,
                                              @Valid @RequestBody ExamRequest req) {
        return ResponseEntity.ok(examService.updateExam(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Map questions into the exam. Body: { "questionIds": [1, 2, 3] }
     * Map.of-style body keeps this simple without another DTO class.
     */
    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExamDto> addQuestions(@PathVariable Long id,
                                                @RequestBody Map<String, List<Long>> body) {
        List<Long> questionIds = body.get("questionIds");
        return ResponseEntity.ok(examService.addQuestionsToExam(id, questionIds));
    }
}
