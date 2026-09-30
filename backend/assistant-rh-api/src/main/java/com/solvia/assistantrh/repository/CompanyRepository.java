package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {

    Optional<CompanyEntity> findFirstByOrderByIdAsc();
}
