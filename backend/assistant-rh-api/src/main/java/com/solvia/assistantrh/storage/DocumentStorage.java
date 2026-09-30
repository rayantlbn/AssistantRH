package com.solvia.assistantrh.storage;

import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * Stockage des fichiers des documents (docs/architecture.md, section 8). Les services ne connaissent que cette
 * interface : passer du disque local à un stockage objet (S3…) se fait en fournissant une autre implémentation.
 * <p>
 * Les chemins manipulés sont relatifs au stockage et toujours générés par l'implémentation, jamais par le client.
 */
public interface DocumentStorage {

    /** Enregistre un PDF pour un candidat sous un nom généré (UUID). Le nom d'origine n'est jamais utilisé. */
    StoredDocument store(Long candidateId, InputStream content);

    /** Contenu d'un fichier précédemment enregistré. */
    Resource load(String path);

    /** Supprime un fichier. Ne lève pas d'erreur s'il n'existe plus : un fichier orphelin est acceptable, pas l'inverse. */
    void delete(String path);
}
