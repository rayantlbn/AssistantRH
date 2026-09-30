package com.solvia.assistantrh.test.integration;

import com.solvia.assistantrh.entity.ApplicationEntity;
import com.solvia.assistantrh.entity.CandidateEntity;
import com.solvia.assistantrh.entity.CompanyEntity;
import com.solvia.assistantrh.entity.JobOfferEntity;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Données fictives pour les tests d'intégration (jamais de données réelles, RGPD-04).
 * Les emails sont rendus uniques pour ne pas dépendre de l'état de la base partagée.
 */
final class TestData {

    private TestData() {
    }

    static CompanyEntity company(TestEntityManager em) {
        return em.persist(CompanyEntity.builder().name("Société Test").build());
    }

    static CandidateEntity candidate(TestEntityManager em, CompanyEntity company) {
        return em.persist(CandidateEntity.builder()
                .firstName("Sara")
                .lastName("Benali")
                .email(uniqueEmail())
                .company(company)
                .build());
    }

    static JobOfferEntity jobOffer(TestEntityManager em, CompanyEntity company) {
        return em.persist(JobOfferEntity.builder()
                .title("Comptable confirmé(e)")
                .status(JobOfferStatus.OPEN)
                .company(company)
                .build());
    }

    static ApplicationEntity application(TestEntityManager em, CandidateEntity candidate, JobOfferEntity jobOffer) {
        Instant now = Instant.now();
        return em.persist(ApplicationEntity.builder()
                .candidate(candidate)
                .jobOffer(jobOffer)
                .appliedAt(now)
                .statusChangedAt(now)
                .build());
    }

    static String uniqueEmail() {
        return "candidat-" + UUID.randomUUID() + "@example.com";
    }

    /** PostgreSQL stocke les dates à la microseconde : on tronque avant de comparer. */
    static Instant micros(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MICROS);
    }

    static String columnValue(TestEntityManager em, String table, String column, Long id) {
        return (String) em.getEntityManager()
                .createNativeQuery("select " + column + " from " + table + " where id = ?1")
                .setParameter(1, id)
                .getSingleResult();
    }
}
