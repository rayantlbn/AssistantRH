package com.solvia.assistantrh.exception;

/**
 * Le candidat a déjà une candidature pour cette offre (APP-02). Traduite en 409.
 */
public class DuplicateApplicationException extends BusinessRuleException {

    public DuplicateApplicationException() {
        super("DUPLICATE_APPLICATION", "Ce candidat a déjà une candidature pour cette offre.");
    }
}
