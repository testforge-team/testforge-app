package com.testforge.result.service;

import com.testforge.common.enums.AttemptStatus;
import com.testforge.result.entity.Result;
import com.testforge.result.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NEW FILE - the safety net for abandoned attempts.
 *
 * THE PROBLEM IT SOLVES
 * A student starts an exam and then closes the browser without submitting.
 * Nobody is left to press Submit, so that attempt would sit as IN_PROGRESS
 * for ever - and because a student may have only one unfinished attempt per
 * exam, they could never start it again either.
 *
 * WHAT IT DOES
 * Every minute it looks for attempts still marked IN_PROGRESS whose deadline
 * has already passed, and closes each one using the answers that were saved
 * before the student left. They are marked EXPIRED so the admin can see how
 * the attempt ended.
 *
 * Note the student does not have to be online for this. If they DO come back,
 * AttemptService handles it immediately instead of waiting for this job.
 */
@Component
@RequiredArgsConstructor
public class AttemptExpiryScheduler {

    private final ResultRepository resultRepository;
    private final GradingService gradingService;

    /**
     * cron = "0 * * * * *" means at second 0 of every minute.
     * A minute is frequent enough for an exam portal and costs almost nothing,
     * because the query is indexed on (status, end_time).
     */
    @Scheduled(cron = "0 * * * * *")
    public void closeExpiredAttempts() {

        List<Result> expired = resultRepository.findByStatusAndEndTimeBefore(
                AttemptStatus.IN_PROGRESS, LocalDateTime.now());

        for (Result attempt : expired) {
            try {
                gradingService.finalizeAttempt(attempt, AttemptStatus.EXPIRED);
            } catch (Exception e) {
                // One bad attempt must not stop the rest, exactly like the
                // email broadcast. We log and carry on.
                System.err.println("Could not close attempt "
                        + attempt.getResultId() + ": " + e.getMessage());
            }
        }
    }
}
