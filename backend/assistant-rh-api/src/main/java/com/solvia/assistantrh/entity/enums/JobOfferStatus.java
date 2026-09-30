package com.solvia.assistantrh.entity.enums;

/**
 * Statut d'une offre. Transitions : DRAFT -> OPEN, OPEN -> CLOSED, CLOSED -> OPEN.
 */
public enum JobOfferStatus {
    DRAFT,
    OPEN,
    CLOSED
}
