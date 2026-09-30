package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.entity.JobOfferEntity;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobOfferRepository extends JpaRepository<JobOfferEntity, Long> {

    Optional<JobOfferEntity> findByIdAndCompanyId(Long id, Long companyId);

    Page<JobOfferEntity> findAllByCompanyId(Long companyId, Pageable pageable);

    Page<JobOfferEntity> findAllByCompanyIdAndStatus(Long companyId, JobOfferStatus status, Pageable pageable);
}
