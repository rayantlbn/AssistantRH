package com.solvia.assistantrh.service;

import com.solvia.assistantrh.entity.CompanyEntity;
import com.solvia.assistantrh.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Point unique d'accès à l'entreprise courante. Phase 1 : l'entreprise unique créée au démarrage.
 * À l'arrivée de la connexion (S1), seule cette classe changera : elle lira l'entreprise de l'utilisateur connecté.
 */
@Component
@RequiredArgsConstructor
public class CurrentCompanyProvider {

    private final CompanyRepository companyRepository;

    public CompanyEntity getCurrentCompany() {
        return companyRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Aucune entreprise n'est configurée"));
    }

    public Long getCurrentCompanyId() {
        return getCurrentCompany().getId();
    }
}
