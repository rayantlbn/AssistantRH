package com.solvia.assistantrh.dto.dashboard;

/**
 * Ligne du tableau de bord (DSH-01) : une offre ouverte et le nombre de ses candidatures par statut.
 */
public record JobOfferDashboardResponse(
        Long jobOfferId,
        String title,
        Long newCount,
        Long interviewCount,
        Long hiredCount,
        Long rejectedCount
) {
}
