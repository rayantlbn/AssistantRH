package com.solvia.assistantrh.exception;

import lombok.Getter;

/**
 * Données invalides détectées par une règle qui ne peut pas s'exprimer en annotation de validation
 * (ex. avis d'entretien saisi avant la date de l'entretien). Traduite en 400.
 */
@Getter
public class InvalidRequestException extends RuntimeException {

    private final String code;

    public InvalidRequestException(String code, String message) {
        super(message);
        this.code = code;
    }
}
