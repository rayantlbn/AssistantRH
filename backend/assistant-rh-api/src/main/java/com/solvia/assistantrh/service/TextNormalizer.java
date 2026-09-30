package com.solvia.assistantrh.service;

import java.util.Locale;

/**
 * Nettoyage des chaînes avant enregistrement (docs/domain.md, Conventions) :
 * espaces retirés en début et fin, chaîne vide enregistrée comme null.
 */
final class TextNormalizer {

    private TextNormalizer() {
    }

    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
