package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.entity.ApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long>, JpaSpecificationExecutor<ApplicationEntity> {

    @EntityGraph(attributePaths = {"candidate", "jobOffer"})
    Optional<ApplicationEntity> findByIdAndCandidate_Company_Id(Long id, Long companyId);

    boolean existsByCandidateIdAndJobOfferId(Long candidateId, Long jobOfferId);

    boolean existsByJobOfferId(Long jobOfferId);

    /** Candidat et offre chargés dans la même requête : ils figurent dans chaque ligne de la réponse. */
    @Override
    @EntityGraph(attributePaths = {"candidate", "jobOffer"})
    Page<ApplicationEntity> findAll(Specification<ApplicationEntity> spec, Pageable pageable);
}
