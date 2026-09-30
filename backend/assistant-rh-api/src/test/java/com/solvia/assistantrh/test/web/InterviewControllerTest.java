package com.solvia.assistantrh.test.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

class InterviewControllerTest extends WebTestSupport {

    private static final String YESTERDAY = Instant.now().minus(1, ChronoUnit.DAYS).toString();
    private static final String TOMORROW = Instant.now().plus(1, ChronoUnit.DAYS).toString();

    @Test
    void create_list_update_delete() throws Exception {
        long applicationId = createApplication(createCandidate(), createOpenJobOffer("Comptable"));

        long id = idOf(postJson("/api/interviews", """
                {"applicationId": %d, "date": "%s", "type": "VIDEO", "participants": "Karim Idrissi"}""".formatted(applicationId, TOMORROW)));

        mockMvc.perform(get("/api/interviews").param("applicationId", String.valueOf(applicationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", contains((int) id)));

        mockMvc.perform(put("/api/interviews/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"date": "%s", "type": "VIDEO", "feedback": "Bonne maîtrise technique.", "outcome": "FAVORABLE"}""".formatted(YESTERDAY)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("FAVORABLE"))
                .andExpect(jsonPath("$.applicationId").value(applicationId));

        mockMvc.perform(delete("/api/interviews/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/interviews/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void feedbackWithoutOutcome_returns400() throws Exception {
        long applicationId = createApplication(createCandidate(), createOpenJobOffer("Comptable"));

        mockMvc.perform(post("/api/interviews").contentType(MediaType.APPLICATION_JSON).content("""
                        {"applicationId": %d, "date": "%s", "type": "PHONE", "feedback": "Compte rendu sans avis"}""".formatted(applicationId, YESTERDAY)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OUTCOME_REQUIRED"));
    }

    @Test
    void outcomeOnFutureInterview_returns400() throws Exception {
        long applicationId = createApplication(createCandidate(), createOpenJobOffer("Comptable"));

        mockMvc.perform(post("/api/interviews").contentType(MediaType.APPLICATION_JSON).content("""
                        {"applicationId": %d, "date": "%s", "type": "PHONE", "outcome": "FAVORABLE"}""".formatted(applicationId, TOMORROW)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INTERVIEW_NOT_HELD"));
    }

    @Test
    void createOnRejectedApplication_returns409() throws Exception {
        long applicationId = createApplication(createCandidate(), createOpenJobOffer("Comptable"));
        mockMvc.perform(put("/api/applications/{id}", applicationId).contentType(MediaType.APPLICATION_JSON).content("""
                {"status": "REJECTED", "appliedAt": "%s"}""".formatted(YESTERDAY))).andExpect(status().isOk());

        mockMvc.perform(post("/api/interviews").contentType(MediaType.APPLICATION_JSON).content("""
                        {"applicationId": %d, "date": "%s", "type": "PHONE"}""".formatted(applicationId, TOMORROW)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("APPLICATION_CLOSED"));
    }

    @Test
    void list_withoutApplicationId_returns400() throws Exception {
        mockMvc.perform(get("/api/interviews")).andExpect(status().isBadRequest());
    }

    @Test
    void create_missingDateAndType_returns400() throws Exception {
        mockMvc.perform(post("/api/interviews").contentType(MediaType.APPLICATION_JSON).content("{\"applicationId\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("date", "type")));
    }
}
