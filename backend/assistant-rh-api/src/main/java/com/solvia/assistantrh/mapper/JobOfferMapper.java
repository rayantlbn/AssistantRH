package com.solvia.assistantrh.mapper;

import com.solvia.assistantrh.dto.joboffer.JobOfferRequest;
import com.solvia.assistantrh.dto.joboffer.JobOfferResponse;
import com.solvia.assistantrh.entity.JobOfferEntity;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Le statut n'est jamais mappé vers l'entité : ses transitions sont contrôlées par JobOfferService.
 */
@Mapper(config = CentralMapperConfig.class)
public interface JobOfferMapper {

    JobOfferResponse toResponse(JobOfferEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "applications", ignore = true)
    JobOfferEntity toEntity(JobOfferRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(JobOfferRequest request, @MappingTarget JobOfferEntity entity);
}
