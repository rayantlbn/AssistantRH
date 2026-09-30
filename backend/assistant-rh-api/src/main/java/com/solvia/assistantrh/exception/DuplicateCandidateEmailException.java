package com.solvia.assistantrh.exception;

/**
 * Un candidat avec le même email existe déjà dans l'entreprise (CAN-02). Traduite en 409.
 */
public class DuplicateCandidateEmailException extends BusinessRuleException {

    public DuplicateCandidateEmailException() {
        super("DUPLICATE_CANDIDATE_EMAIL", "Un candidat avec cet email existe déjà pour cette entreprise.");
    }
}
