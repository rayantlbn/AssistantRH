package com.solvia.assistantrh.test.service;

import com.solvia.assistantrh.dto.application.ApplicationCreateRequest;
import com.solvia.assistantrh.dto.application.ApplicationUpdateRequest;
import com.solvia.assistantrh.dto.interview.InterviewRequest;
import com.solvia.assistantrh.dto.interview.InterviewResponse;
import com.solvia.assistantrh.entity.enums.ApplicationStatus;
import com.solvia.assistantrh.entity.enums.InterviewOutcome;
import com.solvia.assistantrh.entity.enums.InterviewType;
import com.solvia.assistantrh.exception.BusinessRuleException;
import com.solvia.assistantrh.exception.InvalidRequestException;
import com.solvia.assistantrh.service.ApplicationService;
import com.solvia.assistantrh.service.InterviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterviewServiceTest extends ServiceTestSupport {

    private static final Instant TOMORROW = FIXED_NOW.plus(Duration.ofDays(1));

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private ApplicationService applicationService;

    private Long applicationId;

    @BeforeEach
    void createApplication() {
        applicationId = applicationService.create(new ApplicationCreateRequest(createCandidate(), createOpenJobOffer(), null)).id();
    }

    @Test
    void create_plannedInterview_withoutOutcome() {
        InterviewResponse created = interviewService.create(
                new InterviewRequest(applicationId, TOMORROW, InterviewType.VIDEO, " Karim Idrissi, Nadia Tazi ", null, null));

        assertThat(created.id()).isNotNull();
        assertThat(created.applicationId()).isEqualTo(applicationId);
        assertThat(created.participants()).isEqualTo("Karim Idrissi, Nadia Tazi");
        assertThat(created.outcome()).isNull();
    }

    @Test
    void create_doesNotChangeApplicationStatus() {
        interviewService.create(new InterviewRequest(applicationId, TOMORROW, InterviewType.PHONE, null, null, null));

        assertThat(applicationService.findById(applicationId).status()).isEqualTo(ApplicationStatus.NEW);
    }

    @Test
    void update_outcome_onceInterviewHeld() {
        InterviewResponse planned = interviewService.create(
                new InterviewRequest(applicationId, TOMORROW, InterviewType.ONSITE, null, null, null));
        clock.setInstant(TOMORROW.plus(Duration.ofHours(3)));

        InterviewResponse updated = interviewService.update(planned.id(), new InterviewRequest(
                null, TOMORROW, InterviewType.ONSITE, null, "Bonne maîtrise technique.", InterviewOutcome.FAVORABLE));

        assertThat(updated.outcome()).isEqualTo(InterviewOutcome.FAVORABLE);
        assertThat(updated.feedback()).isEqualTo("Bonne maîtrise technique.");
        assertThat(interviewService.findById(planned.id()).outcome()).isEqualTo(InterviewOutcome.FAVORABLE);
    }

    @Test
    void outcome_onFutureInterview_isRefused() {
        assertThatThrownBy(() -> interviewService.create(
                new InterviewRequest(applicationId, TOMORROW, InterviewType.VIDEO, null, null, InterviewOutcome.FAVORABLE)))
                .isInstanceOf(InvalidRequestException.class)
                .extracting("code").isEqualTo("INTERVIEW_NOT_HELD");
    }

    @Test
    void feedback_withoutOutcome_isRefused() {
        Instant yesterday = FIXED_NOW.minus(Duration.ofDays(1));

        assertThatThrownBy(() -> interviewService.create(
                new InterviewRequest(applicationId, yesterday, InterviewType.PHONE, null, "Compte rendu sans avis", null)))
                .isInstanceOf(InvalidRequestException.class)
                .extracting("code").isEqualTo("OUTCOME_REQUIRED");
    }

    @Test
    void create_onRejectedApplication_isRefused() {
        applicationService.update(applicationId, new ApplicationUpdateRequest(ApplicationStatus.REJECTED, FIXED_NOW));

        assertThatThrownBy(() -> interviewService.create(
                new InterviewRequest(applicationId, TOMORROW, InterviewType.PHONE, null, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("APPLICATION_CLOSED");
    }
}
