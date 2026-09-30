package com.solvia.assistantrh.dto.interview;

import com.solvia.assistantrh.entity.enums.InterviewOutcome;
import com.solvia.assistantrh.entity.enums.InterviewType;

import java.time.Instant;

public record InterviewResponse(
        Long id,
        Long applicationId,
        Instant date,
        InterviewType type,
        String participants,
        String feedback,
        InterviewOutcome outcome,
        Instant createdAt,
        Instant updatedAt
) {
}
