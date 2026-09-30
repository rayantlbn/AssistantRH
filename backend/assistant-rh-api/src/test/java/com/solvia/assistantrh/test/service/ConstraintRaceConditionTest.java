package com.solvia.assistantrh.test.service;

import com.solvia.assistantrh.dto.application.ApplicationCreateRequest;
import com.solvia.assistantrh.dto.candidate.CandidateRequest;
import com.solvia.assistantrh.entity.CandidateEntity;
import com.solvia.assistantrh.entity.CompanyEntity;
import com.solvia.assistantrh.entity.JobOfferEntity;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import com.solvia.assistantrh.exception.BusinessRuleException;
import com.solvia.assistantrh.exception.DuplicateApplicationException;
import com.solvia.assistantrh.exception.DuplicateCandidateEmailException;
import com.solvia.assistantrh.mapper.ApplicationMapper;
import com.solvia.assistantrh.mapper.CandidateMapper;
import com.solvia.assistantrh.mapper.JobOfferMapper;
import com.solvia.assistantrh.repository.ApplicationRepository;
import com.solvia.assistantrh.repository.CandidateRepository;
import com.solvia.assistantrh.repository.JobOfferRepository;
import com.solvia.assistantrh.service.ApplicationService;
import com.solvia.assistantrh.service.CandidateService;
import com.solvia.assistantrh.service.CurrentCompanyProvider;
import com.solvia.assistantrh.service.JobOfferService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Cas de concurrence : une requête passe le contrôle exists() puis une autre modifie la base avant l'écriture.
 * La contrainte SQL (unique ou clé étrangère) rejette l'opération ; le service doit traduire cette violation
 * en erreur métier (409) et laisser remonter toute autre violation. La concurrence est simulée par des mocks.
 */
class ConstraintRaceConditionTest {

    private final CurrentCompanyProvider companyProvider = mock(CurrentCompanyProvider.class);
    private final CompanyEntity company = CompanyEntity.builder().name("Société Test").build();

    ConstraintRaceConditionTest() {
        company.setId(1L);
        when(companyProvider.getCurrentCompany()).thenReturn(company);
        when(companyProvider.getCurrentCompanyId()).thenReturn(1L);
    }

    @Test
    void application_uniqueViolationAfterExistsCheck_isTranslatedToDuplicateApplication() {
        ApplicationRepository applications = mock(ApplicationRepository.class);
        ApplicationService service = applicationService(applications);
        when(applications.existsByCandidateIdAndJobOfferId(anyLong(), anyLong())).thenReturn(false);
        when(applications.saveAndFlush(any())).thenThrow(violationOf("uk_applications_candidate_job_offer"));

        assertThatThrownBy(() -> service.create(new ApplicationCreateRequest(10L, 20L, null)))
                .isInstanceOf(DuplicateApplicationException.class);
    }

    @Test
    void application_otherViolation_isNotHidden() {
        ApplicationRepository applications = mock(ApplicationRepository.class);
        ApplicationService service = applicationService(applications);
        when(applications.existsByCandidateIdAndJobOfferId(anyLong(), anyLong())).thenReturn(false);
        when(applications.saveAndFlush(any())).thenThrow(violationOf("some_other_constraint"));

        assertThatThrownBy(() -> service.create(new ApplicationCreateRequest(10L, 20L, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void candidate_uniqueViolationAfterExistsCheck_isTranslatedToDuplicateEmail() {
        CandidateRepository candidates = mock(CandidateRepository.class);
        CandidateMapper mapper = mock(CandidateMapper.class);
        CandidateService service = new CandidateService(candidates, mapper, companyProvider);
        when(candidates.existsByCompanyIdAndEmail(anyLong(), anyString())).thenReturn(false);
        when(mapper.toEntity(any())).thenReturn(CandidateEntity.builder().firstName("Sara").lastName("Benali").email("sara@example.com").build());
        when(candidates.saveAndFlush(any())).thenThrow(violationOf("uk_candidates_company_email"));

        assertThatThrownBy(() -> service.create(new CandidateRequest("Sara", "Benali", "sara@example.com", null, null, null)))
                .isInstanceOf(DuplicateCandidateEmailException.class);
    }

    @Test
    void jobOffer_applicationCreatedAfterExistsCheck_foreignKeyViolationIsTranslatedToBusinessRule() {
        JobOfferRepository jobOffers = mock(JobOfferRepository.class);
        ApplicationRepository applications = mock(ApplicationRepository.class);
        JobOfferService service = new JobOfferService(jobOffers, applications, mock(JobOfferMapper.class), companyProvider);
        JobOfferEntity jobOffer = JobOfferEntity.builder().company(company).status(JobOfferStatus.OPEN).build();
        jobOffer.setId(20L);
        when(jobOffers.findByIdAndCompanyId(20L, 1L)).thenReturn(Optional.of(jobOffer));
        when(applications.existsByJobOfferId(20L)).thenReturn(false);
        doThrow(violationOf("fk_applications_job_offer")).when(jobOffers).flush();

        assertThatThrownBy(() -> service.delete(20L))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("JOB_OFFER_HAS_APPLICATIONS");
    }

    private ApplicationService applicationService(ApplicationRepository applications) {
        CandidateRepository candidates = mock(CandidateRepository.class);
        JobOfferRepository jobOffers = mock(JobOfferRepository.class);
        CandidateEntity candidate = CandidateEntity.builder().company(company).build();
        candidate.setId(10L);
        JobOfferEntity jobOffer = JobOfferEntity.builder().company(company).status(JobOfferStatus.OPEN).build();
        jobOffer.setId(20L);
        when(candidates.findByIdAndCompanyId(10L, 1L)).thenReturn(Optional.of(candidate));
        when(jobOffers.findByIdAndCompanyId(20L, 1L)).thenReturn(Optional.of(jobOffer));
        Clock clock = Clock.fixed(Instant.parse("2026-03-02T09:00:00Z"), ZoneOffset.UTC);
        return new ApplicationService(applications, candidates, jobOffers, mock(ApplicationMapper.class), companyProvider, clock);
    }

    private static DataIntegrityViolationException violationOf(String constraintName) {
        return new DataIntegrityViolationException("could not execute statement",
                new ConstraintViolationException("duplicate key", new SQLException("duplicate key value"), constraintName));
    }
}
