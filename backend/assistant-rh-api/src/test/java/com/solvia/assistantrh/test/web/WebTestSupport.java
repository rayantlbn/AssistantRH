package com.solvia.assistantrh.test.web;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base des tests HTTP : application complète, requêtes via MockMvc, PostgreSQL du docker-compose.
 * Chaque test est annulé en fin d'exécution ; les fichiers uploadés vont dans un dossier temporaire
 * et sont supprimés lors de l'annulation. Données fictives uniquement.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class WebTestSupport {

    static final Path UPLOAD_DIR = createTempDir();

    @Autowired
    protected MockMvc mockMvc;

    @DynamicPropertySource
    static void uploadDir(DynamicPropertyRegistry registry) {
        registry.add("app.storage.upload-dir", UPLOAD_DIR::toString);
    }

    protected long createCandidate() throws Exception {
        return idOf(postJson("/api/candidates", """
                {"firstName": "Sara", "lastName": "Benali", "email": "%s"}""".formatted(uniqueEmail())));
    }

    protected long createOpenJobOffer(String title) throws Exception {
        long id = idOf(postJson("/api/job-offers", """
                {"title": "%s"}""".formatted(title)));
        mockMvc.perform(put("/api/job-offers/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                {"title": "%s", "status": "OPEN"}""".formatted(title))).andExpect(status().isOk());
        return id;
    }

    protected long createApplication(long candidateId, long jobOfferId) throws Exception {
        return idOf(postJson("/api/applications", """
                {"candidateId": %d, "jobOfferId": %d}""".formatted(candidateId, jobOfferId)));
    }

    /** POST JSON attendu en 201 ; renvoie le résultat pour en extraire l'id. */
    protected MvcResult postJson(String url, String body) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
    }

    protected static long idOf(MvcResult result) throws Exception {
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    protected static String uniqueEmail() {
        return "candidat-" + UUID.randomUUID() + "@example.com";
    }

    private static Path createTempDir() {
        try {
            return Files.createTempDirectory("assistant-rh-uploads-test");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
