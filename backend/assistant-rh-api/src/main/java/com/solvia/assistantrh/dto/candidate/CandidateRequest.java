package com.solvia.assistantrh.dto.candidate;

import com.solvia.assistantrh.entity.enums.CandidateSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Création et modification d'un candidat. Tailles alignées sur les colonnes (docs/domain.md).
 * <p>
 * L'email est débarrassé de ses espaces de début et de fin dès la construction, donc avant la validation :
 * un email copié-collé avec un espace parasite est accepté au lieu d'être rejeté en 400.
 * La mise en minuscules reste faite par CandidateService.
 */
public record CandidateRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 30) String phone,
        CandidateSource source,
        String notes
) {

    public CandidateRequest {
        email = email == null ? null : email.strip();
    }
}
