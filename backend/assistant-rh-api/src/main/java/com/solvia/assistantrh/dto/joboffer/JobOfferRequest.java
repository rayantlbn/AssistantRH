package com.solvia.assistantrh.dto.joboffer;

import com.solvia.assistantrh.entity.enums.ContractType;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Création et modification d'une offre. status est ignoré à la création (toujours DRAFT)
 * et laissé inchangé s'il est absent à la modification.
 */
public record JobOfferRequest(
        @NotBlank @Size(max = 150) String title,
        String description,
        @Size(max = 150) String location,
        ContractType contractType,
        JobOfferStatus status
) {
}
