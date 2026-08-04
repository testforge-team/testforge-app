package com.testforge.common.enums;

/**
 * A student's proficiency label for one topic (used by Person D's analytics).
 * Matches the CHECK constraint on student_weaknesses.status.
 */
public enum WeaknessStatus {
    NEEDS_REVISION,  // accuracy below 50% on this topic
    MASTERED         // accuracy above 80% on this topic
}
