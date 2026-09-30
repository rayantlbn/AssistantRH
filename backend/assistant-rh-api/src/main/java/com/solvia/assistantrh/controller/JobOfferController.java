package com.solvia.assistantrh.controller;

import com.solvia.assistantrh.dto.joboffer.JobOfferRequest;
import com.solvia.assistantrh.dto.joboffer.JobOfferResponse;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import com.solvia.assistantrh.service.JobOfferService;
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

@Tag(name = "Offres", description = "Offres d'emploi")
@RestController
@RequestMapping("/api/job-offers")
@RequiredArgsConstructor
public class JobOfferController {

    private final JobOfferService jobOfferService;

    @GetMapping
    @Operation(summary = "Liste paginée des offres, filtrable par statut. Pagination : page (à partir de 0), size (20 par défaut, 100 max), sort (ex. createdAt,desc).")
    public Page<JobOfferResponse> findAll(@Parameter(description = "Filtre par statut de l'offre") @RequestParam(required = false) JobOfferStatus status,
                                          @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return jobOfferService.findAll(status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une offre")
    public JobOfferResponse findById(@PathVariable Long id) {
        return jobOfferService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Création, toujours au statut DRAFT")
    public ResponseEntity<JobOfferResponse> create(@Valid @RequestBody JobOfferRequest request) {
        JobOfferResponse created = jobOfferService.create(request);
        return ResponseEntity.created(Locations.of(created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modification et changement de statut (DRAFT → OPEN → CLOSED → OPEN)")
    public JobOfferResponse update(@PathVariable Long id, @Valid @RequestBody JobOfferRequest request) {
        return jobOfferService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Suppression, refusée (409) si l’offre a des candidatures")
    public void delete(@PathVariable Long id) {
        jobOfferService.delete(id);
    }
}
