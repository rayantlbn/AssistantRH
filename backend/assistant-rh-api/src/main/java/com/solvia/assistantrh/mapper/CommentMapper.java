package com.solvia.assistantrh.mapper;

import com.solvia.assistantrh.dto.comment.CommentRequest;
import com.solvia.assistantrh.dto.comment.CommentResponse;
import com.solvia.assistantrh.entity.CommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface CommentMapper {

    @Mapping(target = "applicationId", source = "application.id")
    CommentResponse toResponse(CommentEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "application", ignore = true)
    CommentEntity toEntity(CommentRequest request);
}
