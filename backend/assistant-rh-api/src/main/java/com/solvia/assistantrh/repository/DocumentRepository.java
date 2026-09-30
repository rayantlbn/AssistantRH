package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    Optional<DocumentEntity> findByIdAndCandidate_Company_Id(Long id, Long companyId);

    Page<DocumentEntity> findAllByCandidateId(Long candidateId, Pageable pageable);
}
