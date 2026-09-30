package com.solvia.assistantrh.dto.interview;

import com.solvia.assistantrh.entity.enums.InterviewOutcome;
import com.solvia.assistantrh.entity.enums.InterviewType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Planification et modification d'un entretien. applicationId est obligatoire à la création
 * (vérifié par le service) et ignoré à la modification.
 */
public record InterviewRequest(
        Long applicationId,
        @NotNull Instant date,
        @NotNull InterviewType type,
        @Size(max = 500) String participants,
        String feedback,
        InterviewOutcome outcome
) {
}
