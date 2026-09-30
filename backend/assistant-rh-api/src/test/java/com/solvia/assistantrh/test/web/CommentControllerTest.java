package com.solvia.assistantrh.test.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest extends WebTestSupport {

    @Test
    void create_list_delete() throws Exception {
        long applicationId = createApplication(createCandidate(), createOpenJobOffer("Comptable"));
        String url = "/api/applications/" + applicationId + "/comments";

        long first = idOf(postJson(url, "{\"content\": \"Premier commentaire\"}"));
        long second = idOf(postJson(url, "{\"content\": \"  Second commentaire  \"}"));

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", contains((int) second, (int) first)))
                .andExpect(jsonPath("$.content[0].content").value("Second commentaire"))
                .andExpect(jsonPath("$.content[0].applicationId").value(applicationId));

        mockMvc.perform(delete("/api/comments/{id}", first)).andExpect(status().isNoContent());
        mockMvc.perform(get(url)).andExpect(jsonPath("$.content[*].id", contains((int) second)));
    }

    @Test
    void put_isNotAllowed() throws Exception {
        long applicationId = createApplication(createCandidate(), createOpenJobOffer("Comptable"));
        long id = idOf(postJson("/api/applications/" + applicationId + "/comments", "{\"content\": \"Note\"}"));

        mockMvc.perform(put("/api/comments/{id}", id).contentType(MediaType.APPLICATION_JSON).content("{\"content\": \"Modifié\"}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void blankContent_returns400() throws Exception {
        long applicationId = createApplication(createCandidate(), createOpenJobOffer("Comptable"));

        mockMvc.perform(post("/api/applications/{id}/comments", applicationId).contentType(MediaType.APPLICATION_JSON).content("{\"content\": \" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("content"));
    }

    @Test
    void unknownApplication_returns404() throws Exception {
        mockMvc.perform(post("/api/applications/{id}/comments", Long.MAX_VALUE).contentType(MediaType.APPLICATION_JSON).content("{\"content\": \"Note\"}"))
                .andExpect(status().isNotFound());
    }
}
