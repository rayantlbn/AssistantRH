package com.solvia.assistantrh.test.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JobOfferControllerTest extends WebTestSupport {

    @Test
    void create_returns201_alwaysAsDraft() throws Exception {
        mockMvc.perform(post("/api/job-offers").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Développeur Java", "location": "Rabat", "contractType": "PERMANENT", "status": "OPEN"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.contractType").value("PERMANENT"));
    }

    @Test
    void create_blankTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/job-offers").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "   "}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    void findAll_filtersByStatus() throws Exception {
        long openId = createOpenJobOffer("Offre ouverte");

        mockMvc.perform(get("/api/job-offers").param("status", "OPEN").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("OPEN"))))
                .andExpect(jsonPath("$.content[*].id", hasItem((int) openId)));
    }

    @Test
    void findAll_unknownStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/job-offers").param("status", "FOO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void update_invalidTransition_returns409() throws Exception {
        long id = createOpenJobOffer("Comptable");

        mockMvc.perform(put("/api/job-offers/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Comptable", "status": "DRAFT"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
    }

    @Test
    void deleteOfferWithApplications_returns409() throws Exception {
        long offerId = createOpenJobOffer("Comptable");
        createApplication(createCandidate(), offerId);

        mockMvc.perform(delete("/api/job-offers/{id}", offerId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("JOB_OFFER_HAS_APPLICATIONS"));
    }

    @Test
    void deleteOfferWithoutApplications_returns204() throws Exception {
        long offerId = createOpenJobOffer("Offre sans candidature");

        mockMvc.perform(delete("/api/job-offers/{id}", offerId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/job-offers/{id}", offerId)).andExpect(status().isNotFound());
    }
}
