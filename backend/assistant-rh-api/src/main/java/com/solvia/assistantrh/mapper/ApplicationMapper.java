package com.solvia.assistantrh.mapper;

import com.solvia.assistantrh.dto.application.ApplicationResponse;
import com.solvia.assistantrh.entity.ApplicationEntity;
import org.mapstruct.Mapper;

/**
 * Sens entité → réponse uniquement. status, appliedAt et statusChangedAt sont écrits
 * exclusivement par ApplicationService : aucune méthode ne mappe une requête vers l'entité.
 */
@Mapper(config = CentralMapperConfig.class)
public interface ApplicationMapper {

    ApplicationResponse toResponse(ApplicationEntity entity);
}
