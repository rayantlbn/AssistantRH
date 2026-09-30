package com.solvia.assistantrh.test.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OpenApiDocumentationTest extends WebTestSupport {

    @Test
    void apiDocs_describeAllEndpoints_withPaginationAndFilters() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths", hasKey("/api/candidates")))
                .andExpect(jsonPath("$.paths", hasKey("/api/dashboard/job-offers")))
                .andExpect(jsonPath("$.paths", hasKey("/api/documents/{id}")))
                .andExpect(jsonPath("$.paths['/api/comments/{id}']", not(hasKey("put"))))
                .andExpect(jsonPath("$.paths['/api/applications'].get.parameters[*].name",
                        hasItems("candidateId", "jobOfferId", "status", "page", "size", "sort")));
    }

    @Test
    void swaggerUi_isExposed() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }
}
