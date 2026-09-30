package com.solvia.assistantrh.test.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CandidateControllerTest extends WebTestSupport {

    @Test
    void create_returns201_withLocationAndBody() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/api/candidates").contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName": "Youssef", "lastName": "Alaoui", "email": "  %s ", "source": "LINKEDIN"}""".formatted(email.toUpperCase())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/candidates/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.source").value("LINKEDIN"))
                .andExpect(jsonPath("$.company").doesNotExist());
    }

    @Test
    void create_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/candidates").contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName": "Sara", "lastName": "Benali", "email": "pas-un-email"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    void create_missingNames_returns400_withEachField() throws Exception {
        mockMvc.perform(post("/api/candidates").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s"}""".formatted(uniqueEmail())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("firstName", "lastName")));
    }

    @Test
    void create_duplicateEmail_returns409() throws Exception {
        String email = uniqueEmail();
        String body = """
                {"firstName": "Sara", "lastName": "Benali", "email": "%s"}""".formatted(email);
        postJson("/api/candidates", body);

        mockMvc.perform(post("/api/candidates").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CANDIDATE_EMAIL"));
    }

    @Test
    void findById_returns200_andUnknownId_returns404() throws Exception {
        long id = createCandidate();

        mockMvc.perform(get("/api/candidates/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Benali"));
        mockMvc.perform(get("/api/candidates/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void findAll_isPaginated_andSearchable() throws Exception {
        long id = createCandidate();
        String email = com.jayway.jsonpath.JsonPath.read(
                mockMvc.perform(get("/api/candidates/{id}", id)).andReturn().getResponse().getContentAsString(), "$.email");

        mockMvc.perform(get("/api/candidates").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page.size").value(1))
                .andExpect(jsonPath("$.page.totalElements").isNumber());
        mockMvc.perform(get("/api/candidates").param("search", email.toUpperCase()))
                .andExpect(jsonPath("$.content[*].id", contains((int) id)));
    }

    @Test
    void update_returns200() throws Exception {
        long id = createCandidate();

        mockMvc.perform(put("/api/candidates/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName": "Sara", "lastName": "El Amrani", "email": "%s", "phone": "+212 600 000 000"}""".formatted(uniqueEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("El Amrani"))
                .andExpect(jsonPath("$.phone").value("+212 600 000 000"));
    }

    @Test
    void delete_returns204_thenCandidateIsGone() throws Exception {
        long id = createCandidate();

        mockMvc.perform(delete("/api/candidates/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/candidates/{id}", id)).andExpect(status().isNotFound());
    }
}
