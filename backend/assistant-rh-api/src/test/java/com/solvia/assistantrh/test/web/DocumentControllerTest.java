package com.solvia.assistantrh.test.web;

import com.solvia.assistantrh.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerTest extends WebTestSupport {

    private static final byte[] PDF = "%PDF-1.4\n1 0 obj << /Type /Catalog >> endobj\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);

    @Test
    void uploadValidPdf_returns201() throws Exception {
        long candidateId = createCandidate();

        mockMvc.perform(multipart("/api/candidates/{id}/documents", candidateId)
                        .file(new MockMultipartFile("file", "CV Sara Benali.pdf", "application/pdf", PDF)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/documents/\\d+")))
                .andExpect(jsonPath("$.fileName").value("CV Sara Benali.pdf"))
                .andExpect(jsonPath("$.type").value("CV"))
                .andExpect(jsonPath("$.uploadedAt").isNotEmpty())
                .andExpect(jsonPath("$.path").doesNotExist());
    }

    @Test
    void uploadPdfLargerThan5Mb_returns400() throws Exception {
        byte[] tooLarge = Arrays.copyOf(PDF, (int) DocumentService.MAX_FILE_SIZE + 1);

        mockMvc.perform(multipart("/api/candidates/{id}/documents", createCandidate())
                        .file(new MockMultipartFile("file", "gros.pdf", "application/pdf", tooLarge)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DOCUMENT_TOO_LARGE"));
    }

    @Test
    void uploadWrongMimeType_returns400() throws Exception {
        mockMvc.perform(multipart("/api/candidates/{id}/documents", createCandidate())
                        .file(new MockMultipartFile("file", "photo.png", "image/png", PDF)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DOCUMENT_TYPE_NOT_ALLOWED"));
    }

    @Test
    void uploadFakePdf_returns400() throws Exception {
        mockMvc.perform(multipart("/api/candidates/{id}/documents", createCandidate())
                        .file(new MockMultipartFile("file", "cv.pdf", "application/pdf", "pas un pdf".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DOCUMENT_TYPE_NOT_ALLOWED"));
    }

    @Test
    void uploadWithPathInFilename_storesUnderGeneratedName() throws Exception {
        long candidateId = createCandidate();

        mockMvc.perform(multipart("/api/candidates/{id}/documents", candidateId)
                        .file(new MockMultipartFile("file", "../../etc/passwd.pdf", "application/pdf", PDF)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("passwd.pdf"));

        try (Stream<java.nio.file.Path> files = Files.walk(UPLOAD_DIR.resolve("candidates/" + candidateId))) {
            assertThat(files.filter(Files::isRegularFile).map(p -> p.getFileName().toString()))
                    .singleElement().asString().matches("[0-9a-f-]{36}\\.pdf");
        }
    }

    @Test
    void uploadForUnknownCandidate_returns404() throws Exception {
        mockMvc.perform(multipart("/api/candidates/{id}/documents", Long.MAX_VALUE)
                        .file(new MockMultipartFile("file", "cv.pdf", "application/pdf", PDF)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listDocuments_returnsMetadataOnly() throws Exception {
        long candidateId = createCandidate();
        long documentId = upload(candidateId);

        mockMvc.perform(get("/api/candidates/{id}/documents", candidateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(documentId))
                .andExpect(jsonPath("$.content[0].fileName").value("cv.pdf"))
                .andExpect(jsonPath("$.content[0].type").value("CV"))
                .andExpect(jsonPath("$.content[0].uploadedAt").isNotEmpty())
                .andExpect(jsonPath("$.content[0].path").doesNotExist());
    }

    @Test
    void download_returns200_withThePdfItself() throws Exception {
        long documentId = upload(createCandidate());

        mockMvc.perform(get("/api/documents/{id}", documentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString("cv.pdf")))
                .andExpect(content().bytes(PDF));
    }

    @Test
    void deleteDocument_returns204_thenDownloadReturns404() throws Exception {
        long documentId = upload(createCandidate());

        mockMvc.perform(delete("/api/documents/{id}", documentId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/documents/{id}", documentId)).andExpect(status().isNotFound());
    }

    private long upload(long candidateId) throws Exception {
        return idOf(mockMvc.perform(multipart("/api/candidates/{id}/documents", candidateId)
                        .file(new MockMultipartFile("file", "cv.pdf", "application/pdf", PDF)))
                .andExpect(status().isCreated())
                .andReturn());
    }
}
