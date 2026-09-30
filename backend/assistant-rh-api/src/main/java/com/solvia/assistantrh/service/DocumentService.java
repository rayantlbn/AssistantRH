package com.solvia.assistantrh.service;

import com.solvia.assistantrh.dto.document.DocumentResponse;
import com.solvia.assistantrh.entity.DocumentEntity;
import com.solvia.assistantrh.exception.ResourceNotFoundException;
import com.solvia.assistantrh.mapper.DocumentMapper;
import com.solvia.assistantrh.repository.CandidateRepository;
import com.solvia.assistantrh.repository.DocumentRepository;
import com.solvia.assistantrh.storage.DocumentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Documents des candidats : métadonnées en base, fichiers dans DocumentStorage (docs/architecture.md, section 8).
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final DocumentRepository documentRepository;
    private final CandidateRepository candidateRepository;
    private final DocumentMapper documentMapper;
    private final DocumentStorage documentStorage;
    private final CurrentCompanyProvider currentCompanyProvider;

    @Transactional(readOnly = true)
    public Page<DocumentResponse> findByCandidate(Long candidateId, Pageable pageable) {
        candidateRepository.findByIdAndCompanyId(candidateId, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", candidateId));
        return documentRepository.findAllByCandidateId(candidateId, Pageables.withDefaultSort(pageable, DEFAULT_SORT))
                .map(documentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(Long id) {
        return documentMapper.toResponse(load(id));
    }

    /** Supprime les métadonnées, puis le fichier une fois la transaction validée. */
    public void delete(Long id) {
        DocumentEntity document = load(id);
        documentRepository.delete(document);
        String path = document.getPath();
        AfterCommit.run(() -> documentStorage.delete(path));
        log.info("Document deleted id={}", id);
    }

    private DocumentEntity load(Long id) {
        return documentRepository.findByIdAndCandidate_Company_Id(id, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
    }
}
