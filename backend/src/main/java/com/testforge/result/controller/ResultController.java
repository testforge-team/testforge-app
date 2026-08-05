package com.testforge.result.controller;

import com.testforge.result.dto.*;
import com.testforge.result.service.AttemptService;
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
 *
 * WHAT IS NEW IN THIS VERSION
 *   POST /api/exams/{id}/start            open or RESUME an attempt
 *   PUT  /api/attempts/{attemptId}/answer save one answer while writing
 *   POST /api/exams/{id}/submit           now takes an autoSubmitted flag
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResultController {

    private final GradingService gradingService;
    private final ResultService resultService;
    private final AttemptService attemptService;

    /**
     * NEW - called by the exam screen every time it opens.
     *
     * The same call covers all three situations, so the frontend does not have
     * to know which one it is in:
     *   first time      -> a new attempt with the full duration
     *   after a refresh -> the same attempt, with the time and answers left
     *   came back late  -> the attempt is graded and expired=true is returned
     */
    @PostMapping("/exams/{id}/start")
    public ResponseEntity<AttemptStateDto> startOrResume(@PathVariable Long id,
                                                         Principal principal) {
        return ResponseEntity.ok(attemptService.startOrResume(principal.getName(), id));
    }

    /**
     * NEW - the auto-save. Called every time the student picks an option.
     *
     * We check ownership first, so a student cannot write answers into
     * somebody else's attempt by guessing an id. The identity comes from the
     * signed token, so it cannot be faked.
     */
    @PutMapping("/attempts/{attemptId}/answer")
    public ResponseEntity<Void> saveAnswer(@PathVariable Long attemptId,
                                           @Valid @RequestBody SaveAnswerRequest request,
                                           Principal principal) {
        if (!attemptService.isOwnedBy(attemptId, principal.getName())) {
            return ResponseEntity.status(403).build();
        }
        attemptService.saveAnswer(attemptId, request.getQuestionId(), request.getSelectedOption());
        return ResponseEntity.ok().build();
    }

    /**
     * Submit the attempt.
     *
     * autoSubmitted is a query parameter, defaulting to false:
     *   /api/exams/5/submit                  -> the student pressed Submit
     *   /api/exams/5/submit?autoSubmitted=true -> the countdown reached zero
     * Both are graded identically; the flag only records HOW it ended.
     */
    @PostMapping("/exams/{id}/submit")
    public ResponseEntity<ResultDto> submit(@PathVariable Long id,
                                            @RequestBody(required = false) SubmitExamRequest request,
                                            @RequestParam(defaultValue = "false") boolean autoSubmitted,
                                            Principal principal) {
        return ResponseEntity.ok(
                gradingService.submitAttempt(principal.getName(), id, request, autoSubmitted));
    }

    /** A student's finished attempts. */
    @GetMapping("/results/{studentId}")
    public ResponseEntity<List<ResultHistoryDto>> history(@PathVariable Long studentId) {
        return ResponseEntity.ok(resultService.getHistory(studentId));
    }

    /** Full detail of one attempt: answer review plus topic breakdown. */
    @GetMapping("/results/detail/{resultId}")
    public ResponseEntity<ResultDetailDto> detail(@PathVariable Long resultId) {
        return ResponseEntity.ok(resultService.getDetail(resultId));
    }
}
