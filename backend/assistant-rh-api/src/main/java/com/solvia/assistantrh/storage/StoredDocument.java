package com.solvia.assistantrh.storage;

/**
 * Résultat d'un enregistrement de fichier : son emplacement relatif dans le stockage
 * (à conserver dans DocumentEntity.path) et sa taille en octets.
 */
public record StoredDocument(String path, long size) {
}
