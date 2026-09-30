package com.solvia.assistantrh.test.integration;

import com.solvia.assistantrh.entity.CandidateEntity;
import com.solvia.assistantrh.entity.DocumentEntity;
import com.solvia.assistantrh.entity.enums.DocumentType;
import com.solvia.assistantrh.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static com.solvia.assistantrh.test.integration.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Métadonnées uniquement : aucun fichier n'est écrit sur disque.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DocumentRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void saveAndFindById() {
        CandidateEntity candidate = candidate(em, company(em));
        String path = storagePath(candidate);

        DocumentEntity saved = documentRepository.save(DocumentEntity.builder()
                .filename("CV Sara Benali.pdf")
                .path(path)
                .type(DocumentType.CV)
                .candidate(candidate)
                .build());
        em.flush();
        em.clear();

        DocumentEntity found = documentRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getFilename()).isEqualTo("CV Sara Benali.pdf");
        assertThat(found.getPath()).isEqualTo(path);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void persistsEnumAsString() {
        CandidateEntity candidate = candidate(em, company(em));
        DocumentEntity document = em.persist(DocumentEntity.builder()
                .filename("lettre.pdf").path(storagePath(candidate)).type(DocumentType.COVER_LETTER).candidate(candidate)
                .build());
        em.flush();
        em.clear();

        assertThat(documentRepository.findById(document.getId()).orElseThrow().getType()).isEqualTo(DocumentType.COVER_LETTER);
        assertThat(columnValue(em, "documents", "type", document.getId())).isEqualTo("COVER_LETTER");
    }

    @Test
    void persistsRelationToCandidate() {
        CandidateEntity candidate = candidate(em, company(em));
        DocumentEntity document = em.persist(DocumentEntity.builder()
                .filename("cv.pdf").path(storagePath(candidate)).type(DocumentType.CV).candidate(candidate).build());
        em.flush();
        em.clear();

        DocumentEntity found = documentRepository.findById(document.getId()).orElseThrow();
        assertThat(found.getCandidate().getId()).isEqualTo(candidate.getId());
        assertThat(found.getCandidate().getDocuments()).extracting(DocumentEntity::getId).containsExactly(document.getId());
    }

    @Test
    void pathIsUnique() {
        CandidateEntity candidate = candidate(em, company(em));
        String path = storagePath(candidate);
        em.persist(DocumentEntity.builder().filename("a.pdf").path(path).type(DocumentType.CV).candidate(candidate).build());
        em.flush();

        DocumentEntity samePath = DocumentEntity.builder().filename("b.pdf").path(path).type(DocumentType.CV).candidate(candidate).build();

        assertThatThrownBy(() -> documentRepository.saveAndFlush(samePath))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static String storagePath(CandidateEntity candidate) {
        return "candidates/" + candidate.getId() + "/" + UUID.randomUUID() + ".pdf";
    }
}
