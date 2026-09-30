package com.solvia.assistantrh.mapper;

import com.solvia.assistantrh.dto.candidate.CandidateRequest;
import com.solvia.assistantrh.dto.candidate.CandidateResponse;
import com.solvia.assistantrh.entity.CandidateEntity;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class)
public interface CandidateMapper {

    CandidateResponse toResponse(CandidateEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "documents", ignore = true)
    CandidateEntity toEntity(CandidateRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(CandidateRequest request, @MappingTarget CandidateEntity entity);
}
