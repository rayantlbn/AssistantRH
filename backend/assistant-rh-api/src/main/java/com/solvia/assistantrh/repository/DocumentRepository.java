package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    Optional<DocumentEntity> findByIdAndCandidate_Company_Id(Long id, Long companyId);

    Page<DocumentEntity> findAllByCandidateId(Long candidateId, Pageable pageable);

    /** Emplacements des fichiers d'un candidat, pour les supprimer du stockage avec lui. */
    @Query("select d.path from DocumentEntity d where d.candidate.id = :candidateId")
    List<String> findPathsByCandidateId(@Param("candidateId") Long candidateId);
}
