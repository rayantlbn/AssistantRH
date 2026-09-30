package com.solvia.assistantrh.test.service;

import com.solvia.assistantrh.dto.candidate.CandidateRequest;
import com.solvia.assistantrh.dto.joboffer.JobOfferRequest;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import com.solvia.assistantrh.service.CandidateService;
import com.solvia.assistantrh.service.JobOfferService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Base des tests de services : contexte Spring complet contre le PostgreSQL du docker-compose,
 * chaque test annulé en fin d'exécution, horloge fixée à FIXED_NOW. Données fictives uniquement.
 */
@SpringBootTest
@Transactional
@Import(ServiceTestSupport.TestClockConfiguration.class)
abstract class ServiceTestSupport {

    /** Dans le passé par rapport à l'horloge système, pour rester compatible avec @PastOrPresent. */
    static final Instant FIXED_NOW = Instant.parse("2026-03-02T09:00:00Z");

    @Autowired
    protected TestClock clock;

    @Autowired
    protected CandidateService candidateService;

    @Autowired
    protected JobOfferService jobOfferService;

    @BeforeEach
    void resetClock() {
        clock.setInstant(FIXED_NOW);
    }

    protected Long createCandidate() {
        return candidateService.create(new CandidateRequest("Sara", "Benali", uniqueEmail(), null, null, null)).id();
    }

    protected Long createOpenJobOffer() {
        Long id = jobOfferService.create(new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, null)).id();
        jobOfferService.update(id, new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, JobOfferStatus.OPEN));
        return id;
    }

    protected static String uniqueEmail() {
        return "candidat-" + UUID.randomUUID() + "@example.com";
    }

    @TestConfiguration
    static class TestClockConfiguration {

        @Bean
        @Primary
        TestClock testClock() {
            return new TestClock(FIXED_NOW);
        }
    }
}
