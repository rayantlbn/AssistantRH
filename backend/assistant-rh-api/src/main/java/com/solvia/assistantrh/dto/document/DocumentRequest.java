package com.solvia.assistantrh.dto.document;

import com.solvia.assistantrh.entity.enums.DocumentType;

/**
 * Métadonnées envoyées avec le fichier lors de l'upload (Jour 6). type vaut CV s'il est absent.
 */
public record DocumentRequest(DocumentType type) {
}
