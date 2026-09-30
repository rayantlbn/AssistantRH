package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.entity.InterviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewRepository extends JpaRepository<InterviewEntity, Long> {

    Optional<InterviewEntity> findByIdAndApplication_Candidate_Company_Id(Long id, Long companyId);

    Page<InterviewEntity> findAllByApplicationId(Long applicationId, Pageable pageable);
}
