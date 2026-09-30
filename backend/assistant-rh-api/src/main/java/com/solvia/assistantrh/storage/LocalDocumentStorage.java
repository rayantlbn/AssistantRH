package com.solvia.assistantrh.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stockage sur disque local, dans le dossier app.storage.upload-dir (variable UPLOAD_DIR, "uploads" par défaut).
 * Organisation : candidates/{candidateId}/{uuid}.pdf.
 */
@Slf4j
@Component
public class LocalDocumentStorage implements DocumentStorage {

    private final Path root;

    public LocalDocumentStorage(@Value("${app.storage.upload-dir}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public StoredDocument store(Long candidateId, InputStream content) {
        String path = "candidates/" + candidateId + "/" + UUID.randomUUID() + ".pdf";
        Path target = resolve(path);
        try {
            Files.createDirectories(target.getParent());
            long size = Files.copy(content, target);
            return new StoredDocument(path, size);
        } catch (IOException ex) {
            throw new UncheckedIOException("Échec de l'enregistrement du document", ex);
        }
    }

    @Override
    public Resource load(String path) {
        Path file = resolve(path);
        if (!Files.isRegularFile(file)) {
            throw new UncheckedIOException(new NoSuchFileException("Fichier de document absent du stockage"));
        }
        return new FileSystemResource(file);
    }

    @Override
    public void delete(String path) {
        try {
            Files.deleteIfExists(resolve(path));
        } catch (IOException ex) {
            log.warn("Document file could not be deleted", ex);
        }
    }

    /** Refuse tout chemin qui sortirait du dossier de stockage une fois normalisé (path traversal). */
    private Path resolve(String path) {
        Path resolved = root.resolve(path).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Chemin en dehors du dossier de stockage");
        }
        return resolved;
    }
}
