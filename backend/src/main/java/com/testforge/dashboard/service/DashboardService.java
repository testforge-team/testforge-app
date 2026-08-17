package com.testforge.dashboard.service;

import com.testforge.analytics.repository.StudentWeaknessRepository;
import com.testforge.common.enums.Role;
import com.testforge.common.enums.WeaknessStatus;
import com.testforge.dashboard.dto.AdminDashboardDto;
import com.testforge.dashboard.dto.StudentDashboardDto;
import com.testforge.exam.repository.ExamRepository;
import com.testforge.question.repository.QuestionRepository;
import com.testforge.result.entity.Result;
import com.testforge.result.entity.StudentAnswer;
import com.testforge.result.repository.ResultRepository;
import com.testforge.result.repository.StudentAnswerRepository;
import com.testforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Dashboard numbers are just simple counts pulled from the OTHER teams'
 * repositories. This is the clearest example of why "dashboard" isn't a
 * separate table - it only aggregates data others already own.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ResultRepository resultRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final StudentWeaknessRepository weaknessRepository;

    @Transactional(readOnly = true)
    public StudentDashboardDto getStudentDashboard(Long studentId) {
        List<Result> myResults = resultRepository.findByUser_UserIdOrderByExamDateDesc(studentId);

        // Average percentage across this student's attempts.
        double avg = 0;
        if (!myResults.isEmpty()) {
            double sum = 0;
            for (Result r : myResults) {
                int total = studentAnswerRepository.findByResult_ResultId(r.getResultId()).size();
                if (total > 0) sum += (r.getFinalScore() * 100.0 / total);
            }
            avg = Math.round((sum / myResults.size()) * 100.0) / 100.0;
        }

        long weakCount = weaknessRepository.findByUser_UserId(studentId).stream()
                .filter(w -> w.getStatus() == WeaknessStatus.NEEDS_REVISION)
                .count();

        return StudentDashboardDto.builder()
                .availableExams(examRepository.count())      // simple version: all exams
                .examsTaken(myResults.size())
                .averagePercentage(avg)
                .weakTopicsCount(weakCount)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboard() {
        long students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .count();

        // Batch average across ALL attempts by everyone.
        List<Result> all = resultRepository.findAll();
        double batchAvg = 0;
        if (!all.isEmpty()) {
            double sum = 0;
            for (Result r : all) {
                int total = studentAnswerRepository.findByResult_ResultId(r.getResultId()).size();
                if (total > 0) sum += (r.getFinalScore() * 100.0 / total);
            }
            batchAvg = Math.round((sum / all.size()) * 100.0) / 100.0;
        }

        return AdminDashboardDto.builder()
                .totalStudents(students)
                .totalExams(examRepository.count())
                .totalQuestions(questionRepository.count())
                .batchAveragePercentage(batchAvg)
                .build();
    }
}
