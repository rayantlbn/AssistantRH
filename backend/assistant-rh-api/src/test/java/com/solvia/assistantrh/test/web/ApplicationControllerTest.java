package com.solvia.assistantrh.test.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

class ApplicationControllerTest extends WebTestSupport {

    @Test
    void create_returns201_withStatusNewAndSummaries() throws Exception {
        long candidateId = createCandidate();
        long offerId = createOpenJobOffer("Comptable");

        mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("""
                        {"candidateId": %d, "jobOfferId": %d}""".formatted(candidateId, offerId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.appliedAt").isNotEmpty())
                .andExpect(jsonPath("$.statusChangedAt").isNotEmpty())
                .andExpect(jsonPath("$.candidate.id").value(candidateId))
                .andExpect(jsonPath("$.jobOffer.title").value("Comptable"));
    }

    @Test
    void create_duplicate_returns409() throws Exception {
        long candidateId = createCandidate();
        long offerId = createOpenJobOffer("Comptable");
        createApplication(candidateId, offerId);

        mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("""
                        {"candidateId": %d, "jobOfferId": %d}""".formatted(candidateId, offerId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_APPLICATION"));
    }

    @Test
    void create_appliedAtInTheFuture_returns400() throws Exception {
        Instant tomorrow = Instant.now().plus(1, ChronoUnit.DAYS);

        mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("""
                        {"candidateId": %d, "jobOfferId": %d, "appliedAt": "%s"}""".formatted(createCandidate(), createOpenJobOffer("Comptable"), tomorrow)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("appliedAt"));
    }

    @Test
    void create_missingIds_returns400() throws Exception {
        mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("candidateId", "jobOfferId")));
    }

    @Test
    void findAll_filtersByCandidateOfferAndStatus() throws Exception {
        long candidateId = createCandidate();
        long offerA = createOpenJobOffer("Offre A");
        long offerB = createOpenJobOffer("Offre B");
        long onA = createApplication(candidateId, offerA);
        long onB = createApplication(candidateId, offerB);
        mockMvc.perform(put("/api/applications/{id}", onB).contentType(MediaType.APPLICATION_JSON).content("""
                {"status": "INTERVIEW", "appliedAt": "%s"}""".formatted(Instant.now().minusSeconds(60)))).andExpect(status().isOk());

        mockMvc.perform(get("/api/applications").param("candidateId", String.valueOf(candidateId)))
                .andExpect(jsonPath("$.content[*].id", containsInAnyOrder((int) onA, (int) onB)));
        mockMvc.perform(get("/api/applications").param("jobOfferId", String.valueOf(offerA)))
                .andExpect(jsonPath("$.content[*].id", contains((int) onA)));
        mockMvc.perform(get("/api/applications").param("candidateId", String.valueOf(candidateId)).param("status", "INTERVIEW"))
                .andExpect(jsonPath("$.content[*].id", contains((int) onB)));
    }

    @Test
    void update_statusChange_returns200() throws Exception {
        long id = createApplication(createCandidate(), createOpenJobOffer("Comptable"));

        mockMvc.perform(put("/api/applications/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"status": "SHORTLISTED", "appliedAt": "%s"}""".formatted(Instant.now().minus(2, ChronoUnit.DAYS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test
    void delete_returns204() throws Exception {
        long id = createApplication(createCandidate(), createOpenJobOffer("Comptable"));

        mockMvc.perform(delete("/api/applications/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/applications/{id}", id)).andExpect(status().isNotFound());
    }
}
