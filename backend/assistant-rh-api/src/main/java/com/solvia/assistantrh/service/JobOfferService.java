package com.solvia.assistantrh.service;

import com.solvia.assistantrh.dto.joboffer.JobOfferRequest;
import com.solvia.assistantrh.dto.joboffer.JobOfferResponse;
import com.solvia.assistantrh.entity.JobOfferEntity;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import com.solvia.assistantrh.exception.BusinessRuleException;
import com.solvia.assistantrh.exception.ResourceNotFoundException;
import com.solvia.assistantrh.mapper.JobOfferMapper;
import com.solvia.assistantrh.repository.ApplicationRepository;
import com.solvia.assistantrh.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.solvia.assistantrh.service.TextNormalizer.trimToNull;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class JobOfferService {

    static final String APPLICATION_FK_CONSTRAINT = "fk_applications_job_offer";
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    /** Transitions autorisées (docs/domain.md, JobOffer). Une offre ne revient jamais en DRAFT. */
    private static final Map<JobOfferStatus, Set<JobOfferStatus>> ALLOWED_TRANSITIONS = Map.of(
            JobOfferStatus.DRAFT, Set.of(JobOfferStatus.OPEN),
            JobOfferStatus.OPEN, Set.of(JobOfferStatus.CLOSED),
            JobOfferStatus.CLOSED, Set.of(JobOfferStatus.OPEN)
    );

    private final JobOfferRepository jobOfferRepository;
    private final ApplicationRepository applicationRepository;
    private final JobOfferMapper jobOfferMapper;
    private final CurrentCompanyProvider currentCompanyProvider;

    /** Une offre est toujours créée au statut DRAFT, quel que soit le statut demandé. */
    public JobOfferResponse create(JobOfferRequest request) {
        JobOfferEntity jobOffer = jobOfferMapper.toEntity(request);
        normalize(jobOffer);
        jobOffer.setStatus(JobOfferStatus.DRAFT);
        jobOffer.setCompany(currentCompanyProvider.getCurrentCompany());

        JobOfferEntity saved = jobOfferRepository.save(jobOffer);
        log.info("Job offer created id={}", saved.getId());
        return jobOfferMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public JobOfferResponse findById(Long id) {
        return jobOfferMapper.toResponse(load(id));
    }

    /** Liste paginée, filtrable par statut (OFF-05). */
    @Transactional(readOnly = true)
    public Page<JobOfferResponse> findAll(JobOfferStatus status, Pageable pageable) {
        Long companyId = currentCompanyProvider.getCurrentCompanyId();
        Pageable page = Pageables.withDefaultSort(pageable, DEFAULT_SORT);
        Page<JobOfferEntity> offers = status == null
                ? jobOfferRepository.findAllByCompanyId(companyId, page)
                : jobOfferRepository.findAllByCompanyIdAndStatus(companyId, status, page);
        return offers.map(jobOfferMapper::toResponse);
    }

    /**
     * Modification et changement de statut. Une offre clôturée ne peut que repasser en OPEN,
     * sans autre modification (OFF-03, OFF-04). Un status absent laisse le statut inchangé.
     */
    public JobOfferResponse update(Long id, JobOfferRequest request) {
        JobOfferEntity jobOffer = load(id);
        JobOfferStatus current = jobOffer.getStatus();
        JobOfferStatus target = request.status() != null ? request.status() : current;

        if (current == JobOfferStatus.CLOSED && contentChanges(jobOffer, request)) {
            throw new BusinessRuleException("JOB_OFFER_CLOSED",
                    "Une offre clôturée ne peut pas être modifiée. Rouvrez-la d'abord.");
        }
        if (current != target && !ALLOWED_TRANSITIONS.get(current).contains(target)) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "Transition de statut interdite : " + current + " → " + target + ".");
        }

        jobOfferMapper.updateEntity(request, jobOffer);
        normalize(jobOffer);
        jobOffer.setStatus(target);
        log.info("Job offer updated id={}", id);
        return jobOfferMapper.toResponse(jobOffer);
    }

    /**
     * OFF-06 : une offre qui a des candidatures ne peut pas être supprimée. Double vérification :
     * contrôle préalable pour le cas normal, puis la clé étrangère des candidatures si une candidature
     * est créée entre le contrôle et la suppression.
     */
    public void delete(Long id) {
        JobOfferEntity jobOffer = load(id);
        if (applicationRepository.existsByJobOfferId(id)) {
            throw jobOfferHasApplications();
        }
        try {
            jobOfferRepository.delete(jobOffer);
            jobOfferRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            if (DataIntegrityViolations.isViolationOf(ex, APPLICATION_FK_CONSTRAINT)) {
                throw jobOfferHasApplications();
            }
            throw ex;
        }
        log.info("Job offer deleted id={}", id);
    }

    private static BusinessRuleException jobOfferHasApplications() {
        return new BusinessRuleException("JOB_OFFER_HAS_APPLICATIONS",
                "Impossible de supprimer une offre qui a des candidatures. Clôturez-la à la place.");
    }

    JobOfferEntity load(Long id) {
        return jobOfferRepository.findByIdAndCompanyId(id, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Offre", id));
    }

    private static boolean contentChanges(JobOfferEntity jobOffer, JobOfferRequest request) {
        return !Objects.equals(jobOffer.getTitle(), trimToNull(request.title()))
                || !Objects.equals(jobOffer.getDescription(), trimToNull(request.description()))
                || !Objects.equals(jobOffer.getLocation(), trimToNull(request.location()))
                || jobOffer.getContractType() != request.contractType();
    }

    private static void normalize(JobOfferEntity jobOffer) {
        jobOffer.setTitle(trimToNull(jobOffer.getTitle()));
        jobOffer.setDescription(trimToNull(jobOffer.getDescription()));
        jobOffer.setLocation(trimToNull(jobOffer.getLocation()));
    }
}
