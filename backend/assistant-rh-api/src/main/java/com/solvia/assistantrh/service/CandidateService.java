package com.solvia.assistantrh.service;

import com.solvia.assistantrh.dto.candidate.CandidateRequest;
import com.solvia.assistantrh.dto.candidate.CandidateResponse;
import com.solvia.assistantrh.entity.CandidateEntity;
import com.solvia.assistantrh.entity.CompanyEntity;
import com.solvia.assistantrh.exception.DuplicateCandidateEmailException;
import com.solvia.assistantrh.exception.ResourceNotFoundException;
import com.solvia.assistantrh.mapper.CandidateMapper;
import com.solvia.assistantrh.repository.CandidateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Locale;

import static com.solvia.assistantrh.service.TextNormalizer.normalizeEmail;
import static com.solvia.assistantrh.service.TextNormalizer.trimToNull;

@Slf4j
@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class CandidateService {

    static final String UNIQUE_EMAIL_CONSTRAINT = "uk_candidates_company_email";
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final CandidateRepository candidateRepository;
    private final CandidateMapper candidateMapper;
    private final CurrentCompanyProvider currentCompanyProvider;

    public CandidateResponse create(@Valid CandidateRequest request) {
        CompanyEntity company = currentCompanyProvider.getCurrentCompany();
        String email = normalizeEmail(request.email());
        if (candidateRepository.existsByCompanyIdAndEmail(company.getId(), email)) {
            throw new DuplicateCandidateEmailException();
        }

        CandidateEntity candidate = candidateMapper.toEntity(request);
        normalize(candidate);
        candidate.setCompany(company);

        CandidateEntity saved = save(candidate);
        log.info("Candidate created id={}", saved.getId());
        return candidateMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CandidateResponse findById(Long id) {
        return candidateMapper.toResponse(load(id));
    }

    /** Liste paginée, du plus récent au plus ancien par défaut. search filtre sur nom, prénom ou email (SRC-01). */
    @Transactional(readOnly = true)
    public Page<CandidateResponse> findAll(String search, Pageable pageable) {
        Long companyId = currentCompanyProvider.getCurrentCompanyId();
        Pageable page = Pageables.withDefaultSort(pageable, DEFAULT_SORT);
        String text = trimToNull(search);
        Page<CandidateEntity> candidates = text == null
                ? candidateRepository.findAllByCompanyId(companyId, page)
                : candidateRepository.search(companyId, "%" + text.toLowerCase(Locale.ROOT) + "%", page);
        return candidates.map(candidateMapper::toResponse);
    }

    public CandidateResponse update(Long id, @Valid CandidateRequest request) {
        CandidateEntity candidate = load(id);
        String email = normalizeEmail(request.email());
        if (candidateRepository.existsByCompanyIdAndEmailAndIdNot(candidate.getCompany().getId(), email, id)) {
            throw new DuplicateCandidateEmailException();
        }

        candidateMapper.updateEntity(request, candidate);
        normalize(candidate);

        CandidateEntity saved = save(candidate);
        log.info("Candidate updated id={}", id);
        return candidateMapper.toResponse(saved);
    }

    /**
     * Supprime le candidat ; ses candidatures (et leurs entretiens et commentaires) et ses documents sont supprimés
     * par la base. La suppression des fichiers sur disque sera ajoutée avec l'upload (Jour 6).
     */
    public void delete(Long id) {
        candidateRepository.delete(load(id));
        log.info("Candidate deleted id={}", id);
    }

    CandidateEntity load(Long id) {
        return candidateRepository.findByIdAndCompanyId(id, currentCompanyProvider.getCurrentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", id));
    }

    /** La contrainte unique (company_id, email) reste la garantie finale si deux requêtes passent le contrôle en même temps. */
    private CandidateEntity save(CandidateEntity candidate) {
        try {
            return candidateRepository.saveAndFlush(candidate);
        } catch (DataIntegrityViolationException ex) {
            if (DataIntegrityViolations.isViolationOf(ex, UNIQUE_EMAIL_CONSTRAINT)) {
                throw new DuplicateCandidateEmailException();
            }
            throw ex;
        }
    }

    private static void normalize(CandidateEntity candidate) {
        candidate.setFirstName(trimToNull(candidate.getFirstName()));
        candidate.setLastName(trimToNull(candidate.getLastName()));
        candidate.setEmail(normalizeEmail(candidate.getEmail()));
        candidate.setPhone(trimToNull(candidate.getPhone()));
        candidate.setNotes(trimToNull(candidate.getNotes()));
    }
}
