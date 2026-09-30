package com.solvia.assistantrh.mapper;

import com.solvia.assistantrh.dto.interview.InterviewRequest;
import com.solvia.assistantrh.dto.interview.InterviewResponse;
import com.solvia.assistantrh.entity.InterviewEntity;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class)
public interface InterviewMapper {

    @Mapping(target = "applicationId", source = "application.id")
    InterviewResponse toResponse(InterviewEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "application", ignore = true)
    InterviewEntity toEntity(InterviewRequest request);

    @InheritConfiguration(name = "toEntity")
    void updateEntity(InterviewRequest request, @MappingTarget InterviewEntity entity);
}
