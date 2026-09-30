package com.solvia.assistantrh.mapper;

import org.mapstruct.Builder;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Configuration commune des mappers :
 * - beans Spring ;
 * - un champ cible non mappé fait échouer la compilation ;
 * - les entités sont construites par constructeur + setters (pas par le builder Lombok),
 *   pour pouvoir ignorer explicitement les champs hérités de BaseEntity.
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        builder = @Builder(disableBuilder = true)
)
public interface CentralMapperConfig {
}
