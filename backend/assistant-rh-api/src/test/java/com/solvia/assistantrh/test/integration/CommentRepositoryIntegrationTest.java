package com.solvia.assistantrh.test.integration;

import com.solvia.assistantrh.entity.ApplicationEntity;
import com.solvia.assistantrh.entity.CommentEntity;
import com.solvia.assistantrh.entity.CompanyEntity;
import com.solvia.assistantrh.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static com.solvia.assistantrh.test.integration.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * CommentEntity n'a pas d'enum : le test vérifie save, findById et la relation vers la candidature.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void saveAndFindById() {
        CompanyEntity company = company(em);
        ApplicationEntity application = application(em, candidate(em, company), jobOffer(em, company));

        CommentEntity saved = commentRepository.save(CommentEntity.builder()
                .content("Bon profil technique, prétentions salariales élevées pour ce poste.")
                .application(application)
                .build());
        em.flush();
        em.clear();

        CommentEntity found = commentRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getContent()).isEqualTo("Bon profil technique, prétentions salariales élevées pour ce poste.");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void persistsRelationToApplication() {
        CompanyEntity company = company(em);
        ApplicationEntity application = application(em, candidate(em, company), jobOffer(em, company));
        CommentEntity comment = em.persist(CommentEntity.builder().content("À rappeler").application(application).build());
        em.flush();
        em.clear();

        CommentEntity found = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(found.getApplication().getId()).isEqualTo(application.getId());
        assertThat(found.getApplication().getComments()).extracting(CommentEntity::getId).containsExactly(comment.getId());
    }
}
