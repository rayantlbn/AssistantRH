package com.solvia.assistantrh.dto.application;

import com.solvia.assistantrh.entity.enums.ApplicationStatus;

import java.time.Instant;

/**
 * Candidature avec un résumé du candidat et de l'offre, pour éviter un appel par ligne côté frontend.
 */
public record ApplicationResponse(
        Long id,
        ApplicationStatus status,
        Instant appliedAt,
        Instant statusChangedAt,
        Instant createdAt,
        Instant updatedAt,
        CandidateSummary candidate,
        JobOfferSummary jobOffer
) {

    public record CandidateSummary(Long id, String firstName, String lastName) {
    }

    public record JobOfferSummary(Long id, String title) {
    }
}
