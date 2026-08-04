package com.testforge.result.controller;

import com.testforge.result.dto.ResultDetailDto;
import com.testforge.result.dto.ResultDto;
import com.testforge.result.dto.ResultHistoryDto;
import com.testforge.result.dto.SubmitExamRequest;
import com.testforge.result.service.GradingService;
import com.testforge.result.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Endpoints for taking exams and viewing results.
 * The submit endpoint lives under /api/exams/{id} because, from the
 * student's point of view, submitting belongs to an exam. That is fine —
 * URL grouping and code ownership don't have to match.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResultController {

    private final GradingService gradingService;
    private final ResultService resultService;

    /**
     * Submit an attempt. principal.getName() is the student's email from the
     * JWT, so nobody can submit on someone else's behalf — the identity comes
     * from the signed token, not from the request body.
     */
    @PostMapping("/exams/{id}/submit")
    public ResponseEntity<ResultDto> submit(@PathVariable Long id,
                                            @Valid @RequestBody SubmitExamRequest request,
                                            Principal principal) {
        return ResponseEntity.ok(gradingService.gradeSubmission(principal.getName(), id, request));
    }

    /** A student's attempt history. */
    @GetMapping("/results/{studentId}")
    public ResponseEntity<List<ResultHistoryDto>> history(@PathVariable Long studentId) {
        return ResponseEntity.ok(resultService.getHistory(studentId));
    }

    /** Full detail of one attempt (answer review + topic breakdown). */
    @GetMapping("/results/detail/{resultId}")
    public ResponseEntity<ResultDetailDto> detail(@PathVariable Long resultId) {
        return ResponseEntity.ok(resultService.getDetail(resultId));
    }
}
