package com.testforge.dashboard.controller;

import com.testforge.dashboard.dto.AdminDashboardDto;
import com.testforge.dashboard.dto.StudentDashboardDto;
import com.testforge.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/student/{id}")
    public ResponseEntity<StudentDashboardDto> student(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.getStudentDashboard(id));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardDto> admin() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }
}
