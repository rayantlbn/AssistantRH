package com.solvia.assistantrh.controller;

import com.solvia.assistantrh.dto.interview.InterviewRequest;
import com.solvia.assistantrh.dto.interview.InterviewResponse;
import com.solvia.assistantrh.service.InterviewService;
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

@Tag(name = "Entretiens", description = "Entretiens d'une candidature")
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /** Entretiens d'une candidature, par date croissante par défaut. */
    @GetMapping
    @Operation(summary = "Entretiens d'une candidature, par date croissante par défaut. Pagination : page (à partir de 0), size (20 par défaut, 100 max), sort (ex. createdAt,desc).")
    public Page<InterviewResponse> findByApplication(@Parameter(description = "Candidature dont on veut les entretiens", required = true) @RequestParam Long applicationId,
                                                     @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return interviewService.findByApplication(applicationId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un entretien")
    public InterviewResponse findById(@PathVariable Long id) {
        return interviewService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Planification (interdite sur une candidature HIRED ou REJECTED)")
    public ResponseEntity<InterviewResponse> create(@Valid @RequestBody InterviewRequest request) {
        InterviewResponse created = interviewService.create(request);
        return ResponseEntity.created(Locations.of(created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modification, saisie du compte rendu et de l'avis une fois l'entretien passé")
    public InterviewResponse update(@PathVariable Long id, @Valid @RequestBody InterviewRequest request) {
        return interviewService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Suppression d'un entretien")
    public void delete(@PathVariable Long id) {
        interviewService.delete(id);
    }
}
