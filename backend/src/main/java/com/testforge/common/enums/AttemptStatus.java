package com.testforge.common.enums;

/**
 * The life cycle of ONE exam attempt.
 *
 * Until now a row in the results table always meant a finished attempt.
 * Now the row is created the moment the student presses Start, so it needs
 * to say which stage it is at.
 *
 * IN_PROGRESS    - the student is writing the exam right now. Answers are
 *                  being saved into student_answers as they click.
 * SUBMITTED      - the student pressed Submit themselves.
 * AUTO_SUBMITTED - the countdown reached zero while the student was on the
 *                  page, so the browser submitted for them.
 * EXPIRED        - the time ran out while the student was NOT on the page
 *                  (they closed the tab, or came back too late). The server
 *                  closed the attempt using whatever answers were saved.
 *
 * SUBMITTED, AUTO_SUBMITTED and EXPIRED are all "finished" - they are graded
 * exactly the same way. We keep them separate only so the admin can see HOW
 * an attempt ended.
 */
public enum AttemptStatus {
    IN_PROGRESS,
    SUBMITTED,
    AUTO_SUBMITTED,
    EXPIRED
}
