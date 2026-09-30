package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.entity.CandidateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<CandidateEntity, Long> {
}
