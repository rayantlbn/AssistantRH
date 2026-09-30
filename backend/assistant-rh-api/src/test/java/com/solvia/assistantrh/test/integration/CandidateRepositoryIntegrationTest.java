package com.solvia.assistantrh.test.integration;

import com.solvia.assistantrh.entity.CandidateEntity;
import com.solvia.assistantrh.entity.CompanyEntity;
import com.solvia.assistantrh.entity.DocumentEntity;
import com.solvia.assistantrh.entity.enums.CandidateSource;
import com.solvia.assistantrh.entity.enums.DocumentType;
import com.solvia.assistantrh.repository.ApplicationRepository;
import com.solvia.assistantrh.repository.CandidateRepository;
import com.solvia.assistantrh.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.UUID;

import static com.solvia.assistantrh.test.integration.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CandidateRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void saveAndFindById() {
        CompanyEntity company = company(em);
        String email = uniqueEmail();

        CandidateEntity saved = candidateRepository.save(CandidateEntity.builder()
                .firstName("Youssef")
                .lastName("Alaoui")
                .email(email)
                .phone("+212 600 000 000")
                .source(CandidateSource.LINKEDIN)
                .notes("Disponible sous un mois")
                .company(company)
                .build());
        em.flush();
        em.clear();

        CandidateEntity found = candidateRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getFirstName()).isEqualTo("Youssef");
        assertThat(found.getLastName()).isEqualTo("Alaoui");
        assertThat(found.getEmail()).isEqualTo(email);
        assertThat(found.getPhone()).isEqualTo("+212 600 000 000");
        assertThat(found.getNotes()).isEqualTo("Disponible sous un mois");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void persistsEnumAsString() {
        CandidateEntity candidate = candidate(em, company(em));
        candidate.setSource(CandidateSource.JOB_BOARD);
        em.flush();
        em.clear();

        assertThat(candidateRepository.findById(candidate.getId()).orElseThrow().getSource())
                .isEqualTo(CandidateSource.JOB_BOARD);
        assertThat(columnValue(em, "candidates", "source", candidate.getId())).isEqualTo("JOB_BOARD");
    }

    @Test
    void persistsRelations() {
        CompanyEntity company = company(em);
        CandidateEntity candidate = candidate(em, company);
        application(em, candidate, jobOffer(em, company));
        em.persist(DocumentEntity.builder()
                .filename("cv.pdf")
                .path("candidates/" + candidate.getId() + "/" + UUID.randomUUID() + ".pdf")
                .type(DocumentType.CV)
                .candidate(candidate)
                .build());
        em.flush();
        em.clear();

        CandidateEntity found = candidateRepository.findById(candidate.getId()).orElseThrow();
        assertThat(found.getCompany().getId()).isEqualTo(company.getId());
        assertThat(found.getApplications()).hasSize(1);
        assertThat(found.getDocuments()).hasSize(1);
    }

    @Test
    void updatedAtChangesOnUpdateButNotCreatedAt() throws InterruptedException {
        CandidateEntity candidate = candidate(em, company(em));
        em.flush();
        Instant createdAt = candidate.getCreatedAt();
        Instant updatedAt = candidate.getUpdatedAt();

        Thread.sleep(10);
        candidate.setPhone("+212 611 111 111");
        em.flush();
        em.clear();

        CandidateEntity found = candidateRepository.findById(candidate.getId()).orElseThrow();
        assertThat(found.getCreatedAt()).isEqualTo(micros(createdAt));
        assertThat(found.getUpdatedAt()).isAfter(updatedAt);
    }

    @Test
    void emailIsUniquePerCompany() {
        CompanyEntity company = company(em);
        CandidateEntity first = candidate(em, company);

        CandidateEntity duplicate = CandidateEntity.builder()
                .firstName("Autre").lastName("Personne").email(first.getEmail()).company(company).build();

        assertThatThrownBy(() -> candidateRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sameEmailIsAllowedInAnotherCompany() {
        CandidateEntity first = candidate(em, company(em));

        candidateRepository.save(CandidateEntity.builder()
                .firstName("Autre").lastName("Personne").email(first.getEmail()).company(company(em)).build());
        candidateRepository.flush();

        assertThat(candidateRepository.findAll()).filteredOn(c -> c.getEmail().equals(first.getEmail())).hasSize(2);
    }

    @Test
    void deletingCandidateCascadesToApplicationsAndDocuments() {
        CompanyEntity company = company(em);
        CandidateEntity candidate = candidate(em, company);
        Long applicationId = application(em, candidate, jobOffer(em, company)).getId();
        Long documentId = em.persist(DocumentEntity.builder()
                .filename("cv.pdf").path("candidates/" + UUID.randomUUID() + ".pdf").type(DocumentType.CV).candidate(candidate)
                .build()).getId();
        em.flush();
        em.clear();

        candidateRepository.deleteById(candidate.getId());
        candidateRepository.flush();
        em.clear();

        assertThat(applicationRepository.findById(applicationId)).isEmpty();
        assertThat(documentRepository.findById(documentId)).isEmpty();
    }
}
