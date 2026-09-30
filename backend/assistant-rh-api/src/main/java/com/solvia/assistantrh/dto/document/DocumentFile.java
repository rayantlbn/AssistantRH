package com.solvia.assistantrh.dto.document;

import org.springframework.core.io.Resource;

/**
 * Fichier à télécharger : son nom d'origine (pour Content-Disposition) et son contenu.
 */
public record DocumentFile(String fileName, Resource content) {
}
