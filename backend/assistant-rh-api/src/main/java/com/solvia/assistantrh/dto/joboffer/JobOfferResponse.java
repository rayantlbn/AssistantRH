package com.solvia.assistantrh.dto.joboffer;

import com.solvia.assistantrh.entity.enums.ContractType;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;

import java.time.Instant;

public record JobOfferResponse(
        Long id,
        String title,
        String description,
        String location,
        ContractType contractType,
        JobOfferStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
