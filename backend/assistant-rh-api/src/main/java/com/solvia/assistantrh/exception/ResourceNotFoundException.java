package com.solvia.assistantrh.exception;

/**
 * Ressource introuvable (candidat, offre, candidature, entretien, commentaire, document). Traduite en 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " introuvable : id=" + id);
    }
}
