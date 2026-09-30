package com.solvia.assistantrh.exception;

import lombok.Getter;

/**
 * Requête valide mais contraire à une règle métier ou à l'état des données. Traduite en 409.
 * Le code est stable et destiné au frontend, le message est affiché à l'utilisateur.
 */
@Getter
public class BusinessRuleException extends RuntimeException {

    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }
}
