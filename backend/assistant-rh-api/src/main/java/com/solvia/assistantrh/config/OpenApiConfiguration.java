package com.solvia.assistantrh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation OpenAPI de l'API : /v3/api-docs (JSON) et /swagger-ui.html (interface).
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI assistantRhOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Assistant RH — API")
                .version("0.0.1")
                .description("""
                        API du MVP Assistant RH : candidats, offres, candidatures, entretiens, commentaires.
                        Erreurs au format {code, message} (et errors pour la validation).
                        Listes paginées : page (à partir de 0), size (20 par défaut, 100 max), sort."""));
    }
}
