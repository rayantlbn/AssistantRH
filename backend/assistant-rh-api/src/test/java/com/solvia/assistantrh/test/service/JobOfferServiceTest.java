package com.solvia.assistantrh.test.service;

import com.solvia.assistantrh.dto.application.ApplicationCreateRequest;
import com.solvia.assistantrh.dto.joboffer.JobOfferRequest;
import com.solvia.assistantrh.dto.joboffer.JobOfferResponse;
import com.solvia.assistantrh.entity.enums.ContractType;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import com.solvia.assistantrh.exception.BusinessRuleException;
import com.solvia.assistantrh.exception.ResourceNotFoundException;
import com.solvia.assistantrh.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobOfferServiceTest extends ServiceTestSupport {

    @Autowired
    private ApplicationService applicationService;

    @Test
    void create_isAlwaysDraft() {
        JobOfferResponse created = jobOfferService.create(
                new JobOfferRequest(" Développeur Java ", null, "Rabat", ContractType.PERMANENT, JobOfferStatus.OPEN));

        assertThat(created.status()).isEqualTo(JobOfferStatus.DRAFT);
        assertThat(created.title()).isEqualTo("Développeur Java");
        assertThat(created.contractType()).isEqualTo(ContractType.PERMANENT);
    }

    @Test
    void findById_returnsJobOffer() {
        Long id = createOpenJobOffer();

        JobOfferResponse found = jobOfferService.findById(id);

        assertThat(found.status()).isEqualTo(JobOfferStatus.OPEN);
        assertThat(found.location()).isEqualTo("Casablanca");
    }

    @Test
    void delete_withoutApplications_removesJobOffer() {
        Long id = createOpenJobOffer();

        jobOfferService.delete(id);

        assertThatThrownBy(() -> jobOfferService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenOfferWithApplication_whenDeleteOffer_thenBusinessRuleException() {
        Long offerId = createOpenJobOffer();
        applicationService.create(new ApplicationCreateRequest(createCandidate(), offerId, null));

        assertThatThrownBy(() -> jobOfferService.delete(offerId))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("JOB_OFFER_HAS_APPLICATIONS");
    }

    /** Tout ce qui sort du chemin brouillon → ouverte → clôturée → rouverte est refusé (docs/domain.md, JobOffer). */
    @ParameterizedTest(name = "{0} → {1} refusée")
    @CsvSource({"DRAFT, CLOSED", "OPEN, DRAFT", "CLOSED, DRAFT"})
    void update_transitionOutsideAllowedPath_isRefused(JobOfferStatus from, JobOfferStatus to) {
        Long id = jobOfferInStatus(from);

        assertThatThrownBy(() -> jobOfferService.update(id, new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, to)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("INVALID_STATUS_TRANSITION");
        assertThat(jobOfferService.findById(id).status()).isEqualTo(from);
    }

    @Test
    void update_fullAllowedPath_draftOpenClosedReopened() {
        Long id = jobOfferInStatus(JobOfferStatus.DRAFT);

        for (JobOfferStatus next : new JobOfferStatus[]{JobOfferStatus.OPEN, JobOfferStatus.CLOSED, JobOfferStatus.OPEN}) {
            JobOfferResponse updated = jobOfferService.update(id, new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, next));
            assertThat(updated.status()).isEqualTo(next);
        }
    }

    private Long jobOfferInStatus(JobOfferStatus status) {
        Long id = jobOfferService.create(new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, null)).id();
        if (status != JobOfferStatus.DRAFT) {
            jobOfferService.update(id, new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, JobOfferStatus.OPEN));
        }
        if (status == JobOfferStatus.CLOSED) {
            jobOfferService.update(id, new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, JobOfferStatus.CLOSED));
        }
        return id;
    }

    @Test
    void update_closedOffer_backToDraft_isRefused() {
        Long id = createOpenJobOffer();
        jobOfferService.update(id, new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, JobOfferStatus.CLOSED));

        assertThatThrownBy(() -> jobOfferService.update(id,
                new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, JobOfferStatus.DRAFT)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("INVALID_STATUS_TRANSITION");
    }

    @Test
    void update_closedOffer_contentChange_isRefused_butReopeningIsAllowed() {
        Long id = createOpenJobOffer();
        jobOfferService.update(id, new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, JobOfferStatus.CLOSED));

        assertThatThrownBy(() -> jobOfferService.update(id,
                new JobOfferRequest("Nouveau titre", null, "Casablanca", null, JobOfferStatus.CLOSED)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("JOB_OFFER_CLOSED");

        JobOfferResponse reopened = jobOfferService.update(id,
                new JobOfferRequest("Comptable confirmé(e)", null, "Casablanca", null, JobOfferStatus.OPEN));
        assertThat(reopened.status()).isEqualTo(JobOfferStatus.OPEN);
    }
}
