package com.solvia.assistantrh.entity.enums;

/**
 * Statut de suivi d'une candidature. HIRED et REJECTED sont finaux mais réversibles.
 */
public enum ApplicationStatus {
    NEW,
    SHORTLISTED,
    INTERVIEW,
    OFFER,
    HIRED,
    REJECTED
}
