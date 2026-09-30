package com.solvia.assistantrh.service;

import com.solvia.assistantrh.dto.dashboard.JobOfferDashboardResponse;
import com.solvia.assistantrh.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DashboardService {

    private final JobOfferRepository jobOfferRepository;
    private final CurrentCompanyProvider currentCompanyProvider;

    /**
     * Offres ouvertes avec leurs compteurs de candidatures (DSH-01), triées par titre.
     * Le tri demandé par l'appelant est ignoré : la requête est groupée par offre.
     */
    @Transactional(readOnly = true)
    public Page<JobOfferDashboardResponse> getOpenJobOffers(Pageable pageable) {
        Pageable page = pageable.isUnpaged() ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("title"));
        return jobOfferRepository.findOpenJobOfferDashboard(currentCompanyProvider.getCurrentCompanyId(), page);
    }
}
