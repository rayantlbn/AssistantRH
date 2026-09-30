package com.solvia.assistantrh.service;

import com.solvia.assistantrh.dto.application.ApplicationCreateRequest;
import com.solvia.assistantrh.dto.application.ApplicationResponse;
import com.solvia.assistantrh.dto.application.ApplicationUpdateRequest;
import com.solvia.assistantrh.entity.ApplicationEntity;
import com.solvia.assistantrh.entity.CandidateEntity;
import com.solvia.assistantrh.entity.JobOfferEntity;
import com.solvia.assistantrh.entity.enums.ApplicationStatus;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import com.solvia.assistantrh.exception.BusinessRuleException;
import com.solvia.assistantrh.exception.DuplicateApplicationException;
import com.solvia.assistantrh.exception.InvalidRequestException;
import com.solvia.assistantrh.exception.ResourceNotFoundException;
import com.solvia.assistantrh.mapper.ApplicationMapper;
import com.solvia.assistantrh.repository.ApplicationRepository;
import com.solvia.assistantrh.repository.CandidateRepository;
import com.solvia.assistantrh.repository.JobOfferRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Cœur du MVP : création des candidatures et suivi de leur statut.
 * status, appliedAt et statusChangedAt ne sont écrits que par ce service.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationService {

    static final String UNIQUE_CANDIDATE_JOB_OFFER_CONSTRAINT = "uk_applications_candidate_job_offer";
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "appliedAt");

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobOfferRepository jobOfferRepository;
    private final ApplicationMapper applicationMapper;
    private final CurrentCompanyProvider currentCompanyProvider;
    private final Clock clock;

    /**
     * Crée une candidature au statut NEW sur une offre ouverte (APP-01, APP-05).
     * appliedAt vaut, à défaut, l'instant de création lu une seule fois sur l'horloge, comme statusChangedAt.
     */
    public ApplicationResponse create(ApplicationCreateRequest request) {
        Long companyId = currentCompanyProvider.getCurrentCompanyId();
        CandidateEntity candidate = candidateRepository.findByIdAndCompanyId(request.candidateId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", request.candidateId()));
        JobOfferEntity jobOffer = jobOfferRepository.findByIdAndCompanyId(request.jobOfferId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre", request.jobOfferId()));

        if (jobOffer.getStatus() != JobOfferStatus.OPEN) {
            throw new BusinessRuleException("JOB_OFFER_NOT_OPEN",
                    "Les candidatures ne sont acceptées que sur une offre ouverte.");
        }
        if (applicationRepository.existsByCandidateIdAndJobOfferId(candidate.getId(), jobOffer.getId())) {
            throw new DuplicateApplicationException();
        }

        Instant now = clock.instant();
        Instant appliedAt = request.appliedAt() != null ? request.appliedAt() : now;
        requireNotInFuture(appliedAt, now);

        ApplicationEntity application = new ApplicationEntity();
        application.setCandidate(candidate);
        application.setJobOffer(jobOffer);
        application.setStatus(ApplicationStatus.NEW);
        application.setAppliedAt(appliedAt);
        application.setStatusChangedAt(now);

        ApplicationEntity saved = save(application);
        log.info("Application created id={}", saved.getId());
        return applicationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id) {
        return applicationMapper.toResponse(load(id));
    }

    /**
     * Liste paginée, filtrable par offre, candidat et statut (DSH-02, DSH-03, SRC-02).
     * Tri par défaut : candidatures les plus récentes (appliedAt) en premier.
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> findAll(Long jobOfferId, Long candidateId, ApplicationStatus status, Pageable pageable) {
        Specification<ApplicationEntity> filter = filter(currentCompanyProvider.getCurrentCompanyId(), jobOfferId, candidateId, status);
        return applicationRepository.findAll(filter, Pageables.withDefaultSort(pageable, DEFAULT_SORT))
                .map(applicationMapper::toResponse);
    }

    /**
     * Change le statut et corrige la date de candidature. statusChangedAt n'est modifié
     * que si le statut change réellement (APP-07).
     */
    public ApplicationResponse update(Long id, ApplicationUpdateRequest request) {
        ApplicationEntity application = load(id);
        Instant now = clock.instant();
        requireNotInFuture(request.appliedAt(), now);

        if (request.status() != application.getStatus()) {
            application.setStatus(request.status());
            application.setStatusChangedAt(now);
            log.info("Application status changed id={} status={}", id, request.status());
        }
        application.setAppliedAt(request.appliedAt());
        return applicationMapper.toResponse(application);
    }

    /** Les entretiens et commentaires de la candidature sont supprimés par la base. */
    public void delete(Long id) {
        applicationRepository.delete(load(id));
        log.info("Application deleted id={}", id);
    }

    ApplicationEntity load(Long id) {
        return applicationRepository.findByIdAndCandidate_Company_Id(id, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));
    }

    /** La contrainte unique (candidate_id, job_offer_id) reste la garantie finale si deux requêtes passent le contrôle en même temps. */
    private ApplicationEntity save(ApplicationEntity application) {
        try {
            return applicationRepository.saveAndFlush(application);
        } catch (DataIntegrityViolationException ex) {
            if (DataIntegrityViolations.isViolationOf(ex, UNIQUE_CANDIDATE_JOB_OFFER_CONSTRAINT)) {
                throw new DuplicateApplicationException();
            }
            throw ex;
        }
    }

    /** Contrôle fait avec l'horloge de l'application, en plus de @PastOrPresent (horloge système) sur le DTO et l'entité. */
    private static void requireNotInFuture(Instant appliedAt, Instant now) {
        if (appliedAt.isAfter(now)) {
            throw new InvalidRequestException("APPLIED_AT_IN_FUTURE", "La date de candidature ne peut pas être dans le futur.");
        }
    }

    private static Specification<ApplicationEntity> filter(Long companyId, Long jobOfferId, Long candidateId, ApplicationStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("candidate").get("company").get("id"), companyId));
            if (jobOfferId != null) {
                predicates.add(cb.equal(root.get("jobOffer").get("id"), jobOfferId));
            }
            if (candidateId != null) {
                predicates.add(cb.equal(root.get("candidate").get("id"), candidateId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
