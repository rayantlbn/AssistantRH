package com.solvia.assistantrh.config;

import com.solvia.assistantrh.entity.CompanyEntity;
import com.solvia.assistantrh.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Phase 1 only
/**
 * Phase 1 uniquement : l'application fonctionne pour une seule entreprise, sans connexion.
 * Au démarrage, si la table companies est vide, une entreprise est créée avec le nom app.company.name.
 * Idempotent : si une entreprise existe déjà, rien n'est créé, quel que soit le nombre de redémarrages.
 * À supprimer à l'arrivée de la connexion (S1), quand les entreprises seront créées explicitement.
 * Voir docs/architecture.md, section 3.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultCompanyInitializer implements ApplicationRunner {

    private final CompanyRepository companyRepository;

    @Value("${app.company.name}")
    private String companyName;

    @Override
    public void run(ApplicationArguments args) {
        // Phase 1 only : création uniquement si aucune entreprise n'existe.
        if (companyRepository.count() == 0) {
            CompanyEntity company = companyRepository.save(CompanyEntity.builder().name(companyName).build());
            log.info("Default company created id={}", company.getId());
        }
    }
}
