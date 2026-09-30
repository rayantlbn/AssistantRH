package com.solvia.assistantrh.dto.document;

import com.solvia.assistantrh.entity.enums.DocumentType;

import java.time.Instant;

/**
 * Métadonnées d'un document. fileName est le nom d'origine envoyé par le client, uploadedAt la date d'upload.
 * Le chemin de stockage n'est jamais exposé.
 */
public record DocumentResponse(Long id, String fileName, DocumentType type, Instant uploadedAt) {
}
