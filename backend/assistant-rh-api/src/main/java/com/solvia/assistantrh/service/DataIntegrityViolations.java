package com.solvia.assistantrh.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Identifie la contrainte SQL à l'origine d'une DataIntegrityViolationException, pour ne traduire
 * en erreur métier que la violation attendue et laisser remonter les autres (erreur 500).
 */
final class DataIntegrityViolations {

    private DataIntegrityViolations() {
    }

    static boolean isViolationOf(DataIntegrityViolationException ex, String constraintName) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && constraintName.equalsIgnoreCase(violation.getConstraintName())) {
                return true;
            }
        }
        return false;
    }
}
