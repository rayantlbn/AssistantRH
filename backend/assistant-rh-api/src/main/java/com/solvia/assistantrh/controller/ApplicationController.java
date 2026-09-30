package com.solvia.assistantrh.controller;

import com.solvia.assistantrh.dto.application.ApplicationCreateRequest;
import com.solvia.assistantrh.dto.application.ApplicationResponse;
import com.solvia.assistantrh.dto.application.ApplicationUpdateRequest;
import com.solvia.assistantrh.entity.enums.ApplicationStatus;
import com.solvia.assistantrh.service.ApplicationService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Candidatures", description = "Candidature d'un candidat sur une offre et suivi de son statut")
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    @Operation(summary = "Liste paginée, filtrable par candidat, offre et statut. Pagination : page (à partir de 0), size (20 par défaut, 100 max), sort (ex. createdAt,desc).")
    public Page<ApplicationResponse> findAll(@Parameter(description = "Filtre par candidat") @RequestParam(required = false) Long candidateId,
                                             @Parameter(description = "Filtre par offre") @RequestParam(required = false) Long jobOfferId,
                                             @Parameter(description = "Filtre par statut de la candidature") @RequestParam(required = false) ApplicationStatus status,
                                             @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return applicationService.findAll(jobOfferId, candidateId, status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une candidature")
    public ApplicationResponse findById(@PathVariable Long id) {
        return applicationService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Création au statut NEW, sur une offre ouverte uniquement")
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationCreateRequest request) {
        ApplicationResponse created = applicationService.create(request);
        return ResponseEntity.created(Locations.of(created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Changement de statut et correction de la date de candidature")
    public ApplicationResponse update(@PathVariable Long id, @Valid @RequestBody ApplicationUpdateRequest request) {
        return applicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Suppression, avec entretiens et commentaires")
    public void delete(@PathVariable Long id) {
        applicationService.delete(id);
    }
}
