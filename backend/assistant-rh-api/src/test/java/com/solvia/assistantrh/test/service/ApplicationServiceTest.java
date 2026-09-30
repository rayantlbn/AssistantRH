package com.solvia.assistantrh.test.service;

import com.solvia.assistantrh.dto.application.ApplicationCreateRequest;
import com.solvia.assistantrh.dto.application.ApplicationResponse;
import com.solvia.assistantrh.dto.application.ApplicationUpdateRequest;
import com.solvia.assistantrh.dto.joboffer.JobOfferRequest;
import com.solvia.assistantrh.entity.enums.ApplicationStatus;
import com.solvia.assistantrh.exception.BusinessRuleException;
import com.solvia.assistantrh.exception.DuplicateApplicationException;
import com.solvia.assistantrh.exception.InvalidRequestException;
import com.solvia.assistantrh.service.ApplicationService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationServiceTest extends ServiceTestSupport {

    @Autowired
    private ApplicationService applicationService;

    @Test
    void create_withoutAppliedAt_usesClockForBothDates() {
        Long candidateId = createCandidate();
        Long offerId = createOpenJobOffer();

        ApplicationResponse created = applicationService.create(new ApplicationCreateRequest(candidateId, offerId, null));

        assertThat(created.status()).isEqualTo(ApplicationStatus.NEW);
        assertThat(created.appliedAt()).isEqualTo(FIXED_NOW);
        assertThat(created.statusChangedAt()).isEqualTo(FIXED_NOW);
        assertThat(created.candidate().id()).isEqualTo(candidateId);
        assertThat(created.candidate().lastName()).isEqualTo("Benali");
        assertThat(created.jobOffer().id()).isEqualTo(offerId);
        assertThat(created.jobOffer().title()).isEqualTo("Comptable confirmé(e)");
    }

    @Test
    void create_withPastAppliedAt_keepsIt_andStatusChangedAtIsCreationInstant() {
        Instant receivedLastWeek = FIXED_NOW.minus(Duration.ofDays(7));

        ApplicationResponse created = applicationService.create(
                new ApplicationCreateRequest(createCandidate(), createOpenJobOffer(), receivedLastWeek));

        assertThat(created.appliedAt()).isEqualTo(receivedLastWeek);
        assertThat(created.statusChangedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void create_sameCandidateAndOffer_throwsDuplicateApplication() {
        Long candidateId = createCandidate();
        Long offerId = createOpenJobOffer();
        applicationService.create(new ApplicationCreateRequest(candidateId, offerId, null));

        assertThatThrownBy(() -> applicationService.create(new ApplicationCreateRequest(candidateId, offerId, null)))
                .isInstanceOf(DuplicateApplicationException.class);
    }

    @Test
    void create_appliedAtAfterApplicationClock_isRefused() {
        // Postérieure à l'horloge de l'application mais antérieure à l'horloge système : c'est le service qui refuse.
        Instant oneHourLater = FIXED_NOW.plus(Duration.ofHours(1));

        assertThatThrownBy(() -> applicationService.create(
                new ApplicationCreateRequest(createCandidate(), createOpenJobOffer(), oneHourLater)))
                .isInstanceOf(InvalidRequestException.class)
                .extracting("code").isEqualTo("APPLIED_AT_IN_FUTURE");
    }

    @Test
    void create_appliedAtInTheFuture_isRejectedByValidation() {
        Instant tomorrow = Instant.now().plus(Duration.ofDays(1));

        assertThatThrownBy(() -> applicationService.create(
                new ApplicationCreateRequest(createCandidate(), createOpenJobOffer(), tomorrow)))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("appliedAt");
    }

    @Test
    void create_onDraftOffer_isRefused() {
        Long draftOfferId = jobOfferService.create(new JobOfferRequest("Brouillon", null, null, null, null)).id();

        assertThatThrownBy(() -> applicationService.create(new ApplicationCreateRequest(createCandidate(), draftOfferId, null)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("JOB_OFFER_NOT_OPEN");
    }

    @Test
    void update_statusChange_updatesStatusChangedAt() {
        ApplicationResponse created = applicationService.create(
                new ApplicationCreateRequest(createCandidate(), createOpenJobOffer(), null));
        Instant twoHoursLater = FIXED_NOW.plus(Duration.ofHours(2));
        clock.setInstant(twoHoursLater);

        ApplicationResponse updated = applicationService.update(created.id(),
                new ApplicationUpdateRequest(ApplicationStatus.SHORTLISTED, created.appliedAt()));

        assertThat(updated.status()).isEqualTo(ApplicationStatus.SHORTLISTED);
        assertThat(updated.statusChangedAt()).isEqualTo(twoHoursLater);
        assertThat(updated.appliedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void update_sameStatus_keepsStatusChangedAt_butCanCorrectAppliedAt() {
        ApplicationResponse created = applicationService.create(
                new ApplicationCreateRequest(createCandidate(), createOpenJobOffer(), null));
        clock.setInstant(FIXED_NOW.plus(Duration.ofHours(2)));
        Instant realReceptionDate = FIXED_NOW.minus(Duration.ofDays(3));

        ApplicationResponse updated = applicationService.update(created.id(),
                new ApplicationUpdateRequest(ApplicationStatus.NEW, realReceptionDate));

        assertThat(updated.statusChangedAt()).isEqualTo(FIXED_NOW);
        assertThat(updated.appliedAt()).isEqualTo(realReceptionDate);
    }
}
