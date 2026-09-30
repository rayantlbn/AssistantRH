package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    Optional<CommentEntity> findByIdAndApplication_Candidate_Company_Id(Long id, Long companyId);

    Page<CommentEntity> findAllByApplicationId(Long applicationId, Pageable pageable);
}
