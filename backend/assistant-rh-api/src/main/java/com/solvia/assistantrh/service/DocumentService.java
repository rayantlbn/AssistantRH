package com.solvia.assistantrh.service;

import com.solvia.assistantrh.dto.document.DocumentFile;
import com.solvia.assistantrh.dto.document.DocumentResponse;
import com.solvia.assistantrh.entity.CandidateEntity;
import com.solvia.assistantrh.entity.DocumentEntity;
import com.solvia.assistantrh.entity.enums.DocumentType;
import com.solvia.assistantrh.exception.InvalidRequestException;
import com.solvia.assistantrh.exception.ResourceNotFoundException;
import com.solvia.assistantrh.mapper.DocumentMapper;
import com.solvia.assistantrh.repository.CandidateRepository;
import com.solvia.assistantrh.repository.DocumentRepository;
import com.solvia.assistantrh.storage.DocumentStorage;
import com.solvia.assistantrh.storage.StoredDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

/**
 * Documents des candidats : métadonnées en base, fichiers dans DocumentStorage (docs/architecture.md, section 8).
 * Aucun contenu binaire n'est stocké en base.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    /** 5 Mo (DOC-01), même valeur que spring.servlet.multipart.max-file-size. */
    public static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final int MAX_FILENAME_LENGTH = 255;
    private static final String DEFAULT_FILENAME = "document.pdf";
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final DocumentRepository documentRepository;
    private final CandidateRepository candidateRepository;
    private final DocumentMapper documentMapper;
    private final DocumentStorage documentStorage;
    private final CurrentCompanyProvider currentCompanyProvider;

    /**
     * Upload d'un PDF pour un candidat. Contrôles : fichier non vide, 5 Mo maximum, type annoncé application/pdf
     * et signature %PDF- en début de fichier. Le nom d'origine n'est conservé qu'en base, pour l'affichage.
     */
    public DocumentResponse upload(Long candidateId, DocumentType type, String originalFilename, String contentType, byte[] content) {
        CandidateEntity candidate = loadCandidate(candidateId);
        checkPdf(contentType, content);

        StoredDocument stored = documentStorage.store(candidateId, new ByteArrayInputStream(content));
        TransactionHooks.afterRollback(() -> documentStorage.delete(stored.path()));

        DocumentEntity document = DocumentEntity.builder()
                .filename(displayName(originalFilename))
                .path(stored.path())
                .type(type != null ? type : DocumentType.CV)
                .candidate(candidate)
                .build();
        DocumentEntity saved = documentRepository.save(document);
        log.info("Document uploaded id={} candidateId={} size={}", saved.getId(), candidateId, stored.size());
        return documentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> findByCandidate(Long candidateId, Pageable pageable) {
        loadCandidate(candidateId);
        return documentRepository.findAllByCandidateId(candidateId, Pageables.withDefaultSort(pageable, DEFAULT_SORT))
                .map(documentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(Long id) {
        return documentMapper.toResponse(load(id));
    }

    /** Le fichier lui-même, pour le téléchargement. */
    @Transactional(readOnly = true)
    public DocumentFile download(Long id) {
        DocumentEntity document = load(id);
        return new DocumentFile(document.getFilename(), documentStorage.load(document.getPath()));
    }

    /** Supprime les métadonnées, puis le fichier une fois la transaction validée. */
    public void delete(Long id) {
        DocumentEntity document = load(id);
        documentRepository.delete(document);
        String path = document.getPath();
        TransactionHooks.afterCommit(() -> documentStorage.delete(path));
        log.info("Document deleted id={}", id);
    }

    private DocumentEntity load(Long id) {
        return documentRepository.findByIdAndCandidate_Company_Id(id, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
    }

    private CandidateEntity loadCandidate(Long candidateId) {
        return candidateRepository.findByIdAndCompanyId(candidateId, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", candidateId));
    }

    private static void checkPdf(String contentType, byte[] content) {
        if (content == null || content.length == 0) {
            throw new InvalidRequestException("DOCUMENT_EMPTY", "Le fichier est vide.");
        }
        if (content.length > MAX_FILE_SIZE) {
            throw new InvalidRequestException("DOCUMENT_TOO_LARGE", "Le fichier dépasse la taille maximale de 5 Mo.");
        }
        if (contentType == null || !PDF_CONTENT_TYPE.equals(contentType.split(";")[0].strip().toLowerCase(Locale.ROOT))) {
            throw new InvalidRequestException("DOCUMENT_TYPE_NOT_ALLOWED", "Seuls les fichiers PDF sont acceptés.");
        }
        if (content.length < PDF_SIGNATURE.length
                || !Arrays.equals(Arrays.copyOf(content, PDF_SIGNATURE.length), PDF_SIGNATURE)) {
            throw new InvalidRequestException("DOCUMENT_TYPE_NOT_ALLOWED", "Le fichier n'est pas un PDF valide.");
        }
    }

    /**
     * Nom affiché : seul le dernier segment est gardé (un éventuel chemin comme ../../x.pdf est ignoré),
     * sans caractères de contrôle, limité à 255 caractères. Il ne sert jamais à construire un chemin sur disque.
     */
    private static String displayName(String originalFilename) {
        if (originalFilename == null) {
            return DEFAULT_FILENAME;
        }
        String name = originalFilename.substring(Math.max(originalFilename.lastIndexOf('/'), originalFilename.lastIndexOf('\\')) + 1);
        name = name.replaceAll("\\p{Cntrl}", "").strip();
        if (name.isEmpty()) {
            return DEFAULT_FILENAME;
        }
        return name.length() > MAX_FILENAME_LENGTH ? name.substring(0, MAX_FILENAME_LENGTH) : name;
    }
}
