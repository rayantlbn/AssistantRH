package com.solvia.assistantrh.dto.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;

/**
 * Création d'une candidature. appliedAt est facultatif : à défaut, l'instant de création lu par le service.
 */
public record ApplicationCreateRequest(
        @NotNull Long candidateId,
        @NotNull Long jobOfferId,
        @PastOrPresent Instant appliedAt
) {
}
