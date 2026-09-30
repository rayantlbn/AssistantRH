package com.solvia.assistantrh.test.service;

import com.solvia.assistantrh.dto.candidate.CandidateRequest;
import com.solvia.assistantrh.dto.candidate.CandidateResponse;
import com.solvia.assistantrh.entity.enums.CandidateSource;
import com.solvia.assistantrh.exception.DuplicateCandidateEmailException;
import com.solvia.assistantrh.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CandidateServiceTest extends ServiceTestSupport {

    @Test
    void create_normalizesInputAndReturnsCandidate() {
        String email = uniqueEmail();

        CandidateResponse created = candidateService.create(new CandidateRequest(
                "  Youssef ", "Alaoui", "  " + email.toUpperCase() + " ", "", CandidateSource.LINKEDIN, "  "));

        assertThat(created.id()).isNotNull();
        assertThat(created.firstName()).isEqualTo("Youssef");
        assertThat(created.email()).isEqualTo(email);
        assertThat(created.phone()).isNull();
        assertThat(created.notes()).isNull();
        assertThat(created.source()).isEqualTo(CandidateSource.LINKEDIN);
        assertThat(created.createdAt()).isNotNull();
    }

    @Test
    void findById_returnsCandidate() {
        Long id = createCandidate();

        assertThat(candidateService.findById(id).lastName()).isEqualTo("Benali");
    }

    @Test
    void findById_unknownId_throwsNotFound() {
        assertThatThrownBy(() -> candidateService.findById(Long.MAX_VALUE)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesCandidate() {
        Long id = createCandidate();

        candidateService.delete(id);

        assertThatThrownBy(() -> candidateService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_sameEmailInSameCompany_ignoringCase_throwsDuplicate() {
        String email = uniqueEmail();
        candidateService.create(new CandidateRequest("Sara", "Benali", email, null, null, null));

        assertThatThrownBy(() -> candidateService.create(new CandidateRequest("Autre", "Personne", email.toUpperCase(), null, null, null)))
                .isInstanceOf(DuplicateCandidateEmailException.class)
                .hasMessage("Un candidat avec cet email existe déjà pour cette entreprise.");
    }

    @Test
    void update_toEmailOfAnotherCandidate_throwsDuplicate() {
        String takenEmail = uniqueEmail();
        candidateService.create(new CandidateRequest("Sara", "Benali", takenEmail, null, null, null));
        Long otherId = createCandidate();

        assertThatThrownBy(() -> candidateService.update(otherId, new CandidateRequest("Sara", "Benali", takenEmail, null, null, null)))
                .isInstanceOf(DuplicateCandidateEmailException.class);
    }

    /** Cas fréquent : email copié-collé depuis un CV avec des espaces parasites. Nettoyé avant la validation, donc accepté. */
    @Test
    void create_emailSurroundedBySpaces_isTrimmedBeforeValidation() {
        String email = uniqueEmail();

        CandidateResponse created = candidateService.create(new CandidateRequest("Sara", "Benali", " \t" + email + "  ", null, null, null));

        assertThat(created.email()).isEqualTo(email);
    }

    @Test
    void create_invalidEmail_isRejectedByValidation() {
        assertThatThrownBy(() -> candidateService.create(new CandidateRequest("Sara", "Benali", "pas-un-email", null, null, null)))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("email");
    }
}
