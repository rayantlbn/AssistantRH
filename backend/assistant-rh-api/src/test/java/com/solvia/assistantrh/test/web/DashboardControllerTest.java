package com.solvia.assistantrh.test.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;

class DashboardControllerTest extends WebTestSupport {

    @Test
    void jobOffers_countsApplicationsByStatus() throws Exception {
        long offerId = createOpenJobOffer("ZZ Dashboard");
        createApplication(createCandidate(), offerId);
        long interview = createApplication(createCandidate(), offerId);
        long hired = createApplication(createCandidate(), offerId);
        long rejected = createApplication(createCandidate(), offerId);
        setStatus(interview, "INTERVIEW");
        setStatus(hired, "HIRED");
        setStatus(rejected, "REJECTED");
        long emptyOfferId = createOpenJobOffer("ZZ Dashboard vide");

        String offer = "$.content[?(@.jobOfferId == " + offerId + ")]";
        String empty = "$.content[?(@.jobOfferId == " + emptyOfferId + ")]";
        mockMvc.perform(get("/api/dashboard/job-offers").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(offer + ".title", contains("ZZ Dashboard")))
                .andExpect(jsonPath(offer + ".newCount", contains(1)))
                .andExpect(jsonPath(offer + ".interviewCount", contains(1)))
                .andExpect(jsonPath(offer + ".hiredCount", contains(1)))
                .andExpect(jsonPath(offer + ".rejectedCount", contains(1)))
                .andExpect(jsonPath(empty + ".newCount", contains(0)));
    }

    @Test
    void jobOffers_excludesDraftOffers() throws Exception {
        long draftId = idOf(postJson("/api/job-offers", "{\"title\": \"ZZ Brouillon\"}"));

        mockMvc.perform(get("/api/dashboard/job-offers").param("size", "100"))
                .andExpect(jsonPath("$.content[*].jobOfferId", not(hasItem((int) draftId))));
    }

    private void setStatus(long applicationId, String status) throws Exception {
        mockMvc.perform(put("/api/applications/{id}", applicationId).contentType(MediaType.APPLICATION_JSON).content("""
                {"status": "%s", "appliedAt": "%s"}""".formatted(status, Instant.now().minusSeconds(60)))).andExpect(status().isOk());
    }
}
