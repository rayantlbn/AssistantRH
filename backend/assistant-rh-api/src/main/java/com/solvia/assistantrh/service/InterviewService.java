package com.solvia.assistantrh.service;

import com.solvia.assistantrh.dto.interview.InterviewRequest;
import com.solvia.assistantrh.dto.interview.InterviewResponse;
import com.solvia.assistantrh.entity.ApplicationEntity;
import com.solvia.assistantrh.entity.InterviewEntity;
import com.solvia.assistantrh.entity.enums.ApplicationStatus;
import com.solvia.assistantrh.exception.BusinessRuleException;
import com.solvia.assistantrh.exception.InvalidRequestException;
import com.solvia.assistantrh.exception.ResourceNotFoundException;
import com.solvia.assistantrh.mapper.InterviewMapper;
import com.solvia.assistantrh.repository.ApplicationRepository;
import com.solvia.assistantrh.repository.InterviewRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.util.Set;

import static com.solvia.assistantrh.service.TextNormalizer.trimToNull;

@Slf4j
@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class InterviewService {

    private static final Set<ApplicationStatus> FINAL_STATUSES = Set.of(ApplicationStatus.HIRED, ApplicationStatus.REJECTED);
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "date");

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewMapper interviewMapper;
    private final CurrentCompanyProvider currentCompanyProvider;
    private final Clock clock;

    /**
     * Planifie un entretien. Ne modifie pas le statut de la candidature ; interdit sur une candidature HIRED ou REJECTED.
     */
    public InterviewResponse create(@Valid InterviewRequest request) {
        if (request.applicationId() == null) {
            throw new InvalidRequestException("APPLICATION_ID_REQUIRED", "La candidature de l'entretien est obligatoire.");
        }
        ApplicationEntity application = loadApplication(request.applicationId());
        if (FINAL_STATUSES.contains(application.getStatus())) {
            throw new BusinessRuleException("APPLICATION_CLOSED",
                    "Impossible de planifier un entretien sur une candidature Embauché ou Refusé.");
        }

        InterviewEntity interview = interviewMapper.toEntity(request);
        normalize(interview);
        checkOutcomeRules(interview);
        interview.setApplication(application);

        InterviewEntity saved = interviewRepository.save(interview);
        log.info("Interview created id={} applicationId={}", saved.getId(), application.getId());
        return interviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InterviewResponse findById(Long id) {
        return interviewMapper.toResponse(load(id));
    }

    /** Entretiens d'une candidature, par date croissante par défaut. */
    @Transactional(readOnly = true)
    public Page<InterviewResponse> findByApplication(Long applicationId, Pageable pageable) {
        loadApplication(applicationId);
        return interviewRepository.findAllByApplicationId(applicationId, Pageables.withDefaultSort(pageable, DEFAULT_SORT))
                .map(interviewMapper::toResponse);
    }

    /**
     * Modifie l'entretien, y compris le compte rendu et l'avis, même si la candidature est terminée.
     * applicationId est ignoré : un entretien ne change pas de candidature.
     */
    public InterviewResponse update(Long id, @Valid InterviewRequest request) {
        InterviewEntity interview = load(id);
        interviewMapper.updateEntity(request, interview);
        normalize(interview);
        checkOutcomeRules(interview);
        log.info("Interview updated id={}", id);
        return interviewMapper.toResponse(interview);
    }

    public void delete(Long id) {
        interviewRepository.delete(load(id));
        log.info("Interview deleted id={}", id);
    }

    private InterviewEntity load(Long id) {
        return interviewRepository.findByIdAndApplication_Candidate_Company_Id(id, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Entretien", id));
    }

    private ApplicationEntity loadApplication(Long applicationId) {
        return applicationRepository.findByIdAndCandidate_Company_Id(applicationId, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", applicationId));
    }

    /**
     * Règles de docs/domain.md (Interview) :
     * - tant que la date est dans le futur, ni avis ni compte rendu ;
     * - un compte rendu exige un avis ; un avis peut être saisi seul.
     */
    private void checkOutcomeRules(InterviewEntity interview) {
        boolean inFuture = interview.getDate().isAfter(clock.instant());
        if (inFuture && (interview.getOutcome() != null || interview.getFeedback() != null)) {
            throw new InvalidRequestException("INTERVIEW_NOT_HELD",
                    "L'avis et le compte rendu ne peuvent être saisis qu'une fois l'entretien passé.");
        }
        if (interview.getFeedback() != null && interview.getOutcome() == null) {
            throw new InvalidRequestException("OUTCOME_REQUIRED", "Un avis est obligatoire dès qu'un compte rendu est saisi.");
        }
    }

    private static void normalize(InterviewEntity interview) {
        interview.setParticipants(trimToNull(interview.getParticipants()));
        interview.setFeedback(trimToNull(interview.getFeedback()));
    }
}
