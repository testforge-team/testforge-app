package com.testforge.analytics.controller;

import com.testforge.analytics.dto.DifficultTopicDto;
import com.testforge.analytics.dto.StudentPerformanceDto;
import com.testforge.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /** A student's own performance analysis. */
    @GetMapping("/student/{id}")
    public ResponseEntity<StudentPerformanceDto> studentPerformance(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getStudentPerformance(id));
    }

    /** Admin-only batch report of the hardest topics. */
    @GetMapping("/difficult-topics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DifficultTopicDto>> difficultTopics() {
        return ResponseEntity.ok(analyticsService.getDifficultTopics());
    }
}
