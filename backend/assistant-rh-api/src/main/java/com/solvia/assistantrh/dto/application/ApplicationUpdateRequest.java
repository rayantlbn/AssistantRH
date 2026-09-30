package com.solvia.assistantrh.dto.application;

import com.solvia.assistantrh.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;

/**
 * Modification d'une candidature : statut et date de candidature. Le candidat et l'offre ne changent jamais.
 */
public record ApplicationUpdateRequest(
        @NotNull ApplicationStatus status,
        @NotNull @PastOrPresent Instant appliedAt
) {
}
