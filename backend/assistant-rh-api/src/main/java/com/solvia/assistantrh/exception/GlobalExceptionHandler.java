package com.solvia.assistantrh.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Traduction centralisée des exceptions en réponses HTTP.
 * Aucun message technique (SQL, stack trace, nom de classe) n'est jamais renvoyé au client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    static final String VALIDATION_MESSAGE = "La requête contient des champs invalides.";
    static final String INTERNAL_MESSAGE = "Une erreur interne est survenue";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, new ApiError("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<ApiError> handleDuplicateApplication(DuplicateApplicationException ex) {
        return error(HttpStatus.CONFLICT, new ApiError(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex) {
        return error(HttpStatus.CONFLICT, new ApiError(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidRequestException ex) {
        return error(HttpStatus.BAD_REQUEST, new ApiError(ex.getCode(), ex.getMessage()));
    }

    /** @Valid sur un corps de requête (controllers). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException ex) {
        List<ApiError.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiError.FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        return validationError(errors);
    }

    /** Contraintes sur des paramètres de méthode de controller. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        List<ApiError.FieldError> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(e -> new ApiError.FieldError(result.getMethodParameter().getParameterName(), e.getDefaultMessage())))
                .toList();
        return validationError(errors);
    }

    /** Contraintes Bean Validation levées hors controller (ex. @PastOrPresent sur une entité à l'enregistrement). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        List<ApiError.FieldError> errors = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldError(lastNode(v.getPropertyPath().toString()), v.getMessage()))
                .toList();
        return validationError(errors);
    }

    /** JSON illisible ou valeur d'énumération inconnue. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, new ApiError("MALFORMED_REQUEST", "Le corps de la requête est illisible."));
    }

    /** Paramètre d'URL ou de requête du mauvais type, ex. ?status=FOO ou /api/candidates/abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST, new ApiError("INVALID_PARAMETER",
                "Valeur invalide pour le paramètre « " + ex.getName() + " »."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        // Exceptions de Spring MVC qui portent déjà leur statut (route inconnue, méthode non supportée…) : ce ne sont pas des erreurs serveur.
        if (ex instanceof ErrorResponse errorResponse && !errorResponse.getStatusCode().is5xxServerError()) {
            return ResponseEntity.status(errorResponse.getStatusCode())
                    .body(new ApiError("HTTP_" + errorResponse.getStatusCode().value(), errorResponse.getBody().getTitle()));
        }
        log.error("Unexpected error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, new ApiError("INTERNAL_ERROR", INTERNAL_MESSAGE));
    }

    private static ResponseEntity<ApiError> validationError(List<ApiError.FieldError> errors) {
        return error(HttpStatus.BAD_REQUEST, new ApiError("VALIDATION_ERROR", VALIDATION_MESSAGE, errors));
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, ApiError body) {
        return ResponseEntity.status(status).body(body);
    }

    /** "create.request.email" → "email" */
    private static String lastNode(String propertyPath) {
        return propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
    }
}
