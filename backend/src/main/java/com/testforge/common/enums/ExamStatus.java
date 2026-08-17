package com.testforge.common.enums;

/**
 * Where an exam is in its own timeline. This is NOT stored in the database -
 * it is calculated from scheduled_at and active_hours every time we build an
 * ExamDto, so it can never become stale.
 *
 *   scheduled_at                    scheduled_at + active_hours
 *        |                                      |
 *  NOT_STARTED  |------- ACTIVE -------|     EXPIRED
 *
 * NOT_STARTED - the exam opens later. The student sees it but cannot start.
 * ACTIVE      - the exam is open right now and can be attempted.
 * EXPIRED     - the window has closed. No new attempts are allowed.
 */
public enum ExamStatus {
    NOT_STARTED,
    ACTIVE,
    EXPIRED
}
