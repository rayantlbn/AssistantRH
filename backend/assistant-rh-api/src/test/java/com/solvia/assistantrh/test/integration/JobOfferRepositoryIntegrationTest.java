package com.solvia.assistantrh.test.integration;

import com.solvia.assistantrh.entity.CompanyEntity;
import com.solvia.assistantrh.entity.JobOfferEntity;
import com.solvia.assistantrh.entity.enums.ContractType;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import com.solvia.assistantrh.repository.JobOfferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static com.solvia.assistantrh.test.integration.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobOfferRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private JobOfferRepository jobOfferRepository;

    @Test
    void saveAndFindById() {
        CompanyEntity company = company(em);

        JobOfferEntity saved = jobOfferRepository.save(JobOfferEntity.builder()
                .title("Développeur Java")
                .description("Équipe produit, stack Spring Boot")
                .location("Casablanca")
                .contractType(ContractType.PERMANENT)
                .company(company)
                .build());
        em.flush();
        em.clear();

        JobOfferEntity found = jobOfferRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("Développeur Java");
        assertThat(found.getDescription()).isEqualTo("Équipe produit, stack Spring Boot");
        assertThat(found.getLocation()).isEqualTo("Casablanca");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void persistsEnumsAsStringWithDraftByDefault() {
        JobOfferEntity offer = jobOfferRepository.save(JobOfferEntity.builder()
                .title("Stagiaire RH").contractType(ContractType.INTERNSHIP).company(company(em)).build());
        em.flush();
        em.clear();

        JobOfferEntity found = jobOfferRepository.findById(offer.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(JobOfferStatus.DRAFT);
        assertThat(found.getContractType()).isEqualTo(ContractType.INTERNSHIP);
        assertThat(columnValue(em, "job_offers", "status", offer.getId())).isEqualTo("DRAFT");
        assertThat(columnValue(em, "job_offers", "contract_type", offer.getId())).isEqualTo("INTERNSHIP");
    }

    @Test
    void persistsRelations() {
        CompanyEntity company = company(em);
        JobOfferEntity offer = jobOffer(em, company);
        application(em, candidate(em, company), offer);
        application(em, candidate(em, company), offer);
        em.flush();
        em.clear();

        JobOfferEntity found = jobOfferRepository.findById(offer.getId()).orElseThrow();
        assertThat(found.getCompany().getId()).isEqualTo(company.getId());
        assertThat(found.getCompany().getJobOffers()).extracting(JobOfferEntity::getId).contains(offer.getId());
        assertThat(found.getApplications()).hasSize(2);
    }

    @Test
    void jobOfferWithApplicationsCannotBeDeleted() {
        CompanyEntity company = company(em);
        JobOfferEntity offer = jobOffer(em, company);
        application(em, candidate(em, company), offer);
        em.flush();
        em.clear();

        jobOfferRepository.deleteById(offer.getId());

        assertThatThrownBy(() -> jobOfferRepository.flush()).isInstanceOf(DataIntegrityViolationException.class);
    }
}
