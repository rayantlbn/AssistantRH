package com.solvia.assistantrh.dto.candidate;

import com.solvia.assistantrh.entity.enums.CandidateSource;

import java.time.Instant;

public record CandidateResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        CandidateSource source,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
