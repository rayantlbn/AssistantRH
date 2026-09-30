package com.solvia.assistantrh.dto.dashboard;

/**
 * Ligne du tableau de bord (DSH-01) : une offre ouverte et le nombre de ses candidatures pour chacun
 * des 6 statuts de ApplicationStatus, pour qu'aucune candidature ne disparaisse des compteurs.
 */
public record JobOfferDashboardResponse(
        Long jobOfferId,
        String title,
        Long newCount,
        Long shortlistedCount,
        Long interviewCount,
        Long offerCount,
        Long hiredCount,
        Long rejectedCount
) {
}
