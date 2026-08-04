package com.testforge.common.enums;

/**
 * Lifecycle of one reminder email attempt (used by Person D's email module,
 * but the enum lives in 'common' because shared code is owned by Person A).
 * Matches the CHECK constraint on email_logs.status in the database.
 */
public enum EmailStatus {
    PENDING,   // created, not yet sent
    SENT,      // handed to the SMTP server successfully
    FAILED     // sending threw an error
}
