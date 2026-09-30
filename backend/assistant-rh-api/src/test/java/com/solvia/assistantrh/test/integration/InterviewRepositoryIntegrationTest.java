package com.solvia.assistantrh.test.integration;

import com.solvia.assistantrh.entity.ApplicationEntity;
import com.solvia.assistantrh.entity.CompanyEntity;
import com.solvia.assistantrh.entity.InterviewEntity;
import com.solvia.assistantrh.entity.enums.InterviewOutcome;
import com.solvia.assistantrh.entity.enums.InterviewType;
import com.solvia.assistantrh.repository.InterviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.solvia.assistantrh.test.integration.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InterviewRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private InterviewRepository interviewRepository;

    @Test
    void saveAndFindById() {
        CompanyEntity company = company(em);
        ApplicationEntity application = application(em, candidate(em, company), jobOffer(em, company));
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);

        InterviewEntity saved = interviewRepository.save(InterviewEntity.builder()
                .date(yesterday)
                .type(InterviewType.ONSITE)
                .participants("Karim Idrissi, Nadia Tazi")
                .feedback("Bonne maîtrise technique, communication à confirmer.")
                .outcome(InterviewOutcome.RESERVED)
                .application(application)
                .build());
        em.flush();
        em.clear();

        InterviewEntity found = interviewRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getDate()).isEqualTo(micros(yesterday));
        assertThat(found.getParticipants()).isEqualTo("Karim Idrissi, Nadia Tazi");
        assertThat(found.getFeedback()).isEqualTo("Bonne maîtrise technique, communication à confirmer.");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void persistsEnumsAsString() {
        CompanyEntity company = company(em);
        InterviewEntity interview = em.persist(InterviewEntity.builder()
                .date(Instant.now())
                .type(InterviewType.VIDEO)
                .outcome(InterviewOutcome.UNFAVORABLE)
                .application(application(em, candidate(em, company), jobOffer(em, company)))
                .build());
        em.flush();
        em.clear();

        InterviewEntity found = interviewRepository.findById(interview.getId()).orElseThrow();
        assertThat(found.getType()).isEqualTo(InterviewType.VIDEO);
        assertThat(found.getOutcome()).isEqualTo(InterviewOutcome.UNFAVORABLE);
        assertThat(columnValue(em, "interviews", "type", interview.getId())).isEqualTo("VIDEO");
        assertThat(columnValue(em, "interviews", "outcome", interview.getId())).isEqualTo("UNFAVORABLE");
    }

    @Test
    void outcomeAndFeedbackAreOptionalWhenPlanned() {
        CompanyEntity company = company(em);
        InterviewEntity planned = em.persist(InterviewEntity.builder()
                .date(Instant.now().plus(3, ChronoUnit.DAYS))
                .type(InterviewType.PHONE)
                .application(application(em, candidate(em, company), jobOffer(em, company)))
                .build());
        em.flush();
        em.clear();

        InterviewEntity found = interviewRepository.findById(planned.getId()).orElseThrow();
        assertThat(found.getOutcome()).isNull();
        assertThat(found.getFeedback()).isNull();
        assertThat(found.getParticipants()).isNull();
    }

    @Test
    void persistsRelationToApplication() {
        CompanyEntity company = company(em);
        ApplicationEntity application = application(em, candidate(em, company), jobOffer(em, company));
        InterviewEntity interview = em.persist(InterviewEntity.builder()
                .date(Instant.now()).type(InterviewType.PHONE).application(application).build());
        em.flush();
        em.clear();

        InterviewEntity found = interviewRepository.findById(interview.getId()).orElseThrow();
        assertThat(found.getApplication().getId()).isEqualTo(application.getId());
        assertThat(found.getApplication().getInterviews()).extracting(InterviewEntity::getId).containsExactly(interview.getId());
    }
}
