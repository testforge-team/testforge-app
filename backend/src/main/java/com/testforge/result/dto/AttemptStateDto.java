package com.testforge.result.dto;

import com.testforge.question.dto.QuestionStudentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * NEW FILE.
 * Everything the exam screen needs, whether it is opening for the first time
 * or re-opening after a refresh. One endpoint answers both cases, which is
 * why the frontend code stays simple.
 *
 * The two fields that make refresh work:
 *   remainingSeconds - calculated by the SERVER from the stored end time, so
 *                      refreshing cannot reset or extend the clock.
 *   savedAnswers     - the answers already stored for this attempt, so the
 *                      selected options and the green navigator buttons come
 *                      back exactly as they were.
 *
 * And the field that handles the "came back too late" case:
 *   expired          - true means the server already closed and graded this
 *                      attempt. The frontend then jumps straight to the
 *                      result page using resultId.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttemptStateDto {

    private Long attemptId;          // the result_id of this attempt
    private Long examId;
    private String title;
    private Integer durationMinutes;

    private long remainingSeconds;   // what the timer should show right now
    private boolean resumed;         // true if this was an existing attempt

    private List<QuestionStudentDto> questions;   // never contains the answers

    /** questionId -> chosen option, for the answers already saved. */
    private Map<Long, String> savedAnswers;

    // ---- set only when the attempt is already over ----
    private boolean expired;
    private Long resultId;
    private String message;
}
