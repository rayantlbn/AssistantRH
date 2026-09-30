package com.solvia.assistantrh.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Corps de toutes les réponses d'erreur de l'API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String code, String message, List<FieldError> errors) {

    public ApiError(String code, String message) {
        this(code, message, null);
    }

    public record FieldError(String field, String message) {
    }
}
