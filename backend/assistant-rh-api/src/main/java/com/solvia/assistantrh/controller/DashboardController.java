package com.solvia.assistantrh.controller;

import com.solvia.assistantrh.dto.dashboard.JobOfferDashboardResponse;
import com.solvia.assistantrh.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tableau de bord", description = "Vues de lecture agrégées")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/job-offers")
    @Operation(
            summary = "Offres ouvertes avec le nombre de candidatures NEW, INTERVIEW, HIRED et REJECTED (DSH-01)",
            description = """
                    Pagination : page (à partir de 0) et size (20 par défaut, 100 max).

                    **Tri non supporté** : les résultats sont toujours triés par titre d'offre (ordre alphabétique). \
                    Le paramètre `sort` est accepté mais ignoré, car la requête est agrégée par offre.

                    Seuls les statuts NEW, INTERVIEW, HIRED et REJECTED sont comptés ; SHORTLISTED et OFFER n'apparaissent pas.""")
    public Page<JobOfferDashboardResponse> openJobOffers(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return dashboardService.getOpenJobOffers(pageable);
    }
}
