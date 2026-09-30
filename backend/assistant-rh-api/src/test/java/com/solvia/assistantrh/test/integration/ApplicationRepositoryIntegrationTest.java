package com.solvia.assistantrh.test.integration;

import com.solvia.assistantrh.entity.ApplicationEntity;
import com.solvia.assistantrh.entity.CandidateEntity;
import com.solvia.assistantrh.entity.CommentEntity;
import com.solvia.assistantrh.entity.CompanyEntity;
import com.solvia.assistantrh.entity.InterviewEntity;
import com.solvia.assistantrh.entity.JobOfferEntity;
import com.solvia.assistantrh.entity.enums.ApplicationStatus;
import com.solvia.assistantrh.entity.enums.InterviewType;
import com.solvia.assistantrh.repository.ApplicationRepository;
import com.solvia.assistantrh.repository.CommentRepository;
import com.solvia.assistantrh.repository.InterviewRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.solvia.assistantrh.test.integration.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApplicationRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void saveAndFindById() {
        CompanyEntity company = company(em);
        CandidateEntity candidate = candidate(em, company);
        JobOfferEntity offer = jobOffer(em, company);
        Instant receivedLastWeek = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant now = Instant.now();

        ApplicationEntity saved = applicationRepository.save(ApplicationEntity.builder()
                .candidate(candidate)
                .jobOffer(offer)
                .appliedAt(receivedLastWeek)
                .statusChangedAt(now)
                .build());
        em.flush();
        em.clear();

        ApplicationEntity found = applicationRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getAppliedAt()).isEqualTo(micros(receivedLastWeek));
        assertThat(found.getStatusChangedAt()).isEqualTo(micros(now));
        assertThat(found.getCreatedAt()).isAfter(found.getAppliedAt());
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void persistsStatusAsStringWithNewByDefault() {
        CompanyEntity company = company(em);
        ApplicationEntity application = application(em, candidate(em, company), jobOffer(em, company));
        em.flush();
        assertThat(columnValue(em, "applications", "status", application.getId())).isEqualTo("NEW");

        application.setStatus(ApplicationStatus.SHORTLISTED);
        em.flush();
        em.clear();

        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.SHORTLISTED);
        assertThat(columnValue(em, "applications", "status", application.getId())).isEqualTo("SHORTLISTED");
    }

    @Test
    void persistsRelations() {
        CompanyEntity company = company(em);
        CandidateEntity candidate = candidate(em, company);
        JobOfferEntity offer = jobOffer(em, company);
        ApplicationEntity application = application(em, candidate, offer);
        em.persist(InterviewEntity.builder()
                .date(Instant.now()).type(InterviewType.VIDEO).application(application).build());
        em.persist(CommentEntity.builder().content("Bon profil").application(application).build());
        em.persist(CommentEntity.builder().content("À rappeler lundi").application(application).build());
        em.flush();
        em.clear();

        ApplicationEntity found = applicationRepository.findById(application.getId()).orElseThrow();
        assertThat(found.getCandidate().getId()).isEqualTo(candidate.getId());
        assertThat(found.getJobOffer().getId()).isEqualTo(offer.getId());
        assertThat(found.getInterviews()).hasSize(1);
        assertThat(found.getComments()).hasSize(2);
    }

    @Test
    void candidateCannotApplyTwiceToSameJobOffer() {
        CompanyEntity company = company(em);
        CandidateEntity candidate = candidate(em, company);
        JobOfferEntity offer = jobOffer(em, company);
        application(em, candidate, offer);
        em.flush();

        Instant now = Instant.now();
        ApplicationEntity duplicate = ApplicationEntity.builder()
                .candidate(candidate).jobOffer(offer).appliedAt(now).statusChangedAt(now).build();

        assertThatThrownBy(() -> applicationRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void appliedAtCannotBeInTheFuture() {
        CompanyEntity company = company(em);
        Instant tomorrow = Instant.now().plus(1, ChronoUnit.DAYS);

        assertThatThrownBy(() -> applicationRepository.saveAndFlush(ApplicationEntity.builder()
                .candidate(candidate(em, company))
                .jobOffer(jobOffer(em, company))
                .appliedAt(tomorrow)
                .statusChangedAt(Instant.now())
                .build()))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("appliedAt");
    }

    @Test
    void deletingApplicationCascadesToInterviewsAndComments() {
        CompanyEntity company = company(em);
        ApplicationEntity application = application(em, candidate(em, company), jobOffer(em, company));
        Long interviewId = em.persist(InterviewEntity.builder()
                .date(Instant.now()).type(InterviewType.PHONE).application(application).build()).getId();
        Long commentId = em.persist(CommentEntity.builder().content("Note").application(application).build()).getId();
        em.flush();
        em.clear();

        applicationRepository.deleteById(application.getId());
        applicationRepository.flush();
        em.clear();

        assertThat(interviewRepository.findById(interviewId)).isEmpty();
        assertThat(commentRepository.findById(commentId)).isEmpty();
    }
}
