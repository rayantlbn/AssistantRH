package com.solvia.assistantrh.service;

import com.solvia.assistantrh.dto.comment.CommentRequest;
import com.solvia.assistantrh.dto.comment.CommentResponse;
import com.solvia.assistantrh.entity.ApplicationEntity;
import com.solvia.assistantrh.entity.CommentEntity;
import com.solvia.assistantrh.exception.ResourceNotFoundException;
import com.solvia.assistantrh.mapper.CommentMapper;
import com.solvia.assistantrh.repository.ApplicationRepository;
import com.solvia.assistantrh.repository.CommentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static com.solvia.assistantrh.service.TextNormalizer.trimToNull;

/**
 * Notes internes sur une candidature. Pas de modification (on supprime et on réécrit), pas d'auteur en Phase 1.
 */
@Slf4j
@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class CommentService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final CommentRepository commentRepository;
    private final ApplicationRepository applicationRepository;
    private final CommentMapper commentMapper;
    private final CurrentCompanyProvider currentCompanyProvider;

    public CommentResponse create(Long applicationId, @Valid CommentRequest request) {
        ApplicationEntity application = loadApplication(applicationId);
        CommentEntity comment = commentMapper.toEntity(request);
        comment.setContent(trimToNull(comment.getContent()));
        comment.setApplication(application);

        CommentEntity saved = commentRepository.save(comment);
        log.info("Comment created id={} applicationId={}", saved.getId(), applicationId);
        return commentMapper.toResponse(saved);
    }

    /** Commentaires d'une candidature, du plus récent au plus ancien par défaut. */
    @Transactional(readOnly = true)
    public Page<CommentResponse> findByApplication(Long applicationId, Pageable pageable) {
        loadApplication(applicationId);
        return commentRepository.findAllByApplicationId(applicationId, Pageables.withDefaultSort(pageable, DEFAULT_SORT))
                .map(commentMapper::toResponse);
    }

    public void delete(Long id) {
        CommentEntity comment = commentRepository.findByIdAndApplication_Candidate_Company_Id(id, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire", id));
        commentRepository.delete(comment);
        log.info("Comment deleted id={}", id);
    }

    private ApplicationEntity loadApplication(Long applicationId) {
        return applicationRepository.findByIdAndCandidate_Company_Id(applicationId, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", applicationId));
    }
}
