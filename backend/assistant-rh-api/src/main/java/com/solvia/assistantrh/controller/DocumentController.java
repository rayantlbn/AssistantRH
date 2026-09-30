package com.solvia.assistantrh.controller;

import com.solvia.assistantrh.dto.document.DocumentFile;
import com.solvia.assistantrh.dto.document.DocumentResponse;
import com.solvia.assistantrh.entity.enums.DocumentType;
import com.solvia.assistantrh.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Tag(name = "Documents", description = "CV et documents des candidats (PDF, 5 Mo maximum)")
@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/api/candidates/{candidateId}/documents")
    @Operation(summary = "Métadonnées des documents d'un candidat (id, fileName, type, uploadedAt), du plus récent au plus ancien. "
            + "Pagination : page, size, sort.")
    public Page<DocumentResponse> findByCandidate(@PathVariable Long candidateId,
                                                  @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return documentService.findByCandidate(candidateId, pageable);
    }

    @PostMapping(value = "/api/candidates/{candidateId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload d'un PDF (5 Mo maximum). Le fichier est stocké sous un nom généré, jamais sous le nom envoyé.")
    public ResponseEntity<DocumentResponse> upload(@PathVariable Long candidateId,
                                                   @Parameter(description = "Fichier PDF") @RequestPart("file") MultipartFile file,
                                                   @Parameter(description = "Nature du document (CV par défaut)")
                                                   @RequestParam(required = false) DocumentType type) {
        DocumentResponse created = documentService.upload(candidateId, type, file.getOriginalFilename(), file.getContentType(), bytes(file));
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/documents/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/api/documents/{id}")
    @Operation(summary = "Téléchargement du fichier PDF lui-même (les métadonnées passent par la liste des documents du candidat)")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DocumentFile file = documentService.download(id);
        ContentDisposition disposition = ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(file.content());
    }

    @DeleteMapping("/api/documents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Suppression du document et de son fichier")
    public void delete(@PathVariable Long id) {
        documentService.delete(id);
    }

    private static byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException("Lecture du fichier envoyé impossible", ex);
        }
    }
}
