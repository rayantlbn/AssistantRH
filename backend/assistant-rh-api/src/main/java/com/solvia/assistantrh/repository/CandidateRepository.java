package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.entity.CandidateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<CandidateEntity, Long> {

    Optional<CandidateEntity> findByIdAndCompanyId(Long id, Long companyId);

    Page<CandidateEntity> findAllByCompanyId(Long companyId, Pageable pageable);

    /** pattern : texte en minuscules entouré de %, construit par le service. */
    @Query("""
            select c from CandidateEntity c
            where c.company.id = :companyId
              and (lower(c.firstName) like :pattern or lower(c.lastName) like :pattern or c.email like :pattern)
            """)
    Page<CandidateEntity> search(@Param("companyId") Long companyId, @Param("pattern") String pattern, Pageable pageable);

    boolean existsByCompanyIdAndEmail(Long companyId, String email);

    boolean existsByCompanyIdAndEmailAndIdNot(Long companyId, String email, Long id);
}
