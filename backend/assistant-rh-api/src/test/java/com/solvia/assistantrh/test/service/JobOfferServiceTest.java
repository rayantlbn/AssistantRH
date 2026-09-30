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
