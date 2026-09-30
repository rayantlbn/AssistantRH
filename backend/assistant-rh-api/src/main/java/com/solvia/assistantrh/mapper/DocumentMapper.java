package com.solvia.assistantrh.mapper;

import com.solvia.assistantrh.dto.document.DocumentResponse;
import com.solvia.assistantrh.entity.DocumentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Sens entité → réponse uniquement. L'entité est construite par DocumentService lors de l'upload,
 * car path est généré par le serveur.
 */
@Mapper(config = CentralMapperConfig.class)
public interface DocumentMapper {

    @Mapping(target = "fileName", source = "filename")
    @Mapping(target = "uploadedAt", source = "createdAt")
    DocumentResponse toResponse(DocumentEntity entity);
}
