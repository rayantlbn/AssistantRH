package com.solvia.assistantrh.controller;

import com.solvia.assistantrh.dto.document.DocumentResponse;
import com.solvia.assistantrh.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Documents", description = "CV et documents des candidats")
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
}
