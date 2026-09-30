package com.solvia.assistantrh.test.service;

import com.solvia.assistantrh.exception.ApiError;
import com.solvia.assistantrh.exception.BusinessRuleException;
import com.solvia.assistantrh.exception.DuplicateApplicationException;
import com.solvia.assistantrh.exception.GlobalExceptionHandler;
import com.solvia.assistantrh.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Format des réponses d'erreur, et surtout : aucun détail technique ne sort dans une réponse 500.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unexpectedException_returnsGenericBody_withoutTechnicalDetails() {
        Exception sqlError = new DataIntegrityViolationException(
                "org.postgresql.util.PSQLException: ERROR: null value in column \"email\" of relation \"candidates\"");

        ResponseEntity<ApiError> response = handler.handleUnexpected(sqlError);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(new ApiError("INTERNAL_ERROR", "Une erreur interne est survenue"));
        assertThat(response.getBody().toString()).doesNotContain("PSQLException", "candidates", "email");
    }

    @Test
    void springErrorWithOwnStatus_isNotTurnedInto500() {
        ResponseEntity<ApiError> response = handler.handleUnexpected(new ErrorResponseException(HttpStatus.METHOD_NOT_ALLOWED));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void notFound_returns404() {
        ResponseEntity<ApiError> response = handler.handleNotFound(new ResourceNotFoundException("Candidat", 42L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void businessRules_return409_withTheirCode() {
        assertThat(handler.handleDuplicateApplication(new DuplicateApplicationException()).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<ApiError> response = handler.handleBusinessRule(new BusinessRuleException("JOB_OFFER_HAS_APPLICATIONS", "…"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("JOB_OFFER_HAS_APPLICATIONS");
    }
}
