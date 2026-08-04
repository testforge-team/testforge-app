package com.testforge.question.controller;

import com.testforge.question.dto.QuestionAdminDto;
import com.testforge.question.dto.QuestionRequest;
import com.testforge.question.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * /api/questions — the question bank. ENTIRELY admin-only:
 * students never browse the bank; they only ever receive questions
 * through the exam-attempt endpoint (which uses the student DTO).
 */
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")   // class-level: applies to every method below
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionAdminDto>> getAllQuestions(
            @RequestParam(required = false) Long topicId) {
        // Optional filter: /api/questions?topicId=3
        if (topicId != null) {
            return ResponseEntity.ok(questionService.getQuestionsByTopic(topicId));
        }
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @PostMapping
    public ResponseEntity<QuestionAdminDto> createQuestion(@Valid @RequestBody QuestionRequest req) {
        return ResponseEntity.ok(questionService.createQuestion(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionAdminDto> updateQuestion(@PathVariable Long id,
                                                           @Valid @RequestBody QuestionRequest req) {
        return ResponseEntity.ok(questionService.updateQuestion(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
