package com.solvia.assistantrh.test.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;

class DashboardControllerTest extends WebTestSupport {

    /** Les 6 statuts sont comptés : 1 NEW, 2 SHORTLISTED, 1 INTERVIEW, 1 OFFER, 1 HIRED, 3 REJECTED = 9 candidatures. */
    @Test
    void jobOffers_countsApplicationsForAllSixStatuses() throws Exception {
        long offerId = createOpenJobOffer("ZZ Dashboard");
        createApplication(createCandidate(), offerId);
        for (String status : new String[]{"SHORTLISTED", "SHORTLISTED", "INTERVIEW", "OFFER", "HIRED", "REJECTED", "REJECTED", "REJECTED"}) {
            setStatus(createApplication(createCandidate(), offerId), status);
        }
        long emptyOfferId = createOpenJobOffer("ZZ Dashboard vide");

        String offer = "$.content[?(@.jobOfferId == " + offerId + ")]";
        String empty = "$.content[?(@.jobOfferId == " + emptyOfferId + ")]";
        mockMvc.perform(get("/api/dashboard/job-offers").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(offer + ".title", contains("ZZ Dashboard")))
                .andExpect(jsonPath(offer + ".newCount", contains(1)))
                .andExpect(jsonPath(offer + ".shortlistedCount", contains(2)))
                .andExpect(jsonPath(offer + ".interviewCount", contains(1)))
                .andExpect(jsonPath(offer + ".offerCount", contains(1)))
                .andExpect(jsonPath(offer + ".hiredCount", contains(1)))
                .andExpect(jsonPath(offer + ".rejectedCount", contains(3)))
                .andExpect(jsonPath(empty + ".newCount", contains(0)))
                .andExpect(jsonPath(empty + ".shortlistedCount", contains(0)))
                .andExpect(jsonPath(empty + ".offerCount", contains(0)));
    }

    /** Contrat figé pour le frontend : exactement ces champs, dans cet ordre. */
    @Test
    void jobOffers_responseContract() throws Exception {
        long offerId = createOpenJobOffer("ZZ Contrat");

        String body = mockMvc.perform(get("/api/dashboard/job-offers").param("size", "100"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        java.util.Map<String, Object> row = com.jayway.jsonpath.JsonPath.<java.util.List<java.util.Map<String, Object>>>read(
                body, "$.content[?(@.jobOfferId == " + offerId + ")]").get(0);

        org.assertj.core.api.Assertions.assertThat(row.keySet()).containsExactly(
                "jobOfferId", "title", "newCount", "shortlistedCount", "interviewCount", "offerCount", "hiredCount", "rejectedCount");
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
