package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.dto.dashboard.JobOfferDashboardResponse;
import com.solvia.assistantrh.entity.JobOfferEntity;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobOfferRepository extends JpaRepository<JobOfferEntity, Long> {

    Optional<JobOfferEntity> findByIdAndCompanyId(Long id, Long companyId);

    Page<JobOfferEntity> findAllByCompanyId(Long companyId, Pageable pageable);

    Page<JobOfferEntity> findAllByCompanyIdAndStatus(Long companyId, JobOfferStatus status, Pageable pageable);

    /**
     * Tableau de bord (DSH-01) : offres ouvertes avec le nombre de candidatures pour chacun des 6 statuts,
     * calculé en une seule requête groupée, sans charger les candidatures.
     */
    @Query(value = """
            select new com.solvia.assistantrh.dto.dashboard.JobOfferDashboardResponse(
                j.id,
                j.title,
                coalesce(sum(case when a.status = com.solvia.assistantrh.entity.enums.ApplicationStatus.NEW then 1 else 0 end), 0),
                coalesce(sum(case when a.status = com.solvia.assistantrh.entity.enums.ApplicationStatus.SHORTLISTED then 1 else 0 end), 0),
                coalesce(sum(case when a.status = com.solvia.assistantrh.entity.enums.ApplicationStatus.INTERVIEW then 1 else 0 end), 0),
                coalesce(sum(case when a.status = com.solvia.assistantrh.entity.enums.ApplicationStatus.OFFER then 1 else 0 end), 0),
                coalesce(sum(case when a.status = com.solvia.assistantrh.entity.enums.ApplicationStatus.HIRED then 1 else 0 end), 0),
                coalesce(sum(case when a.status = com.solvia.assistantrh.entity.enums.ApplicationStatus.REJECTED then 1 else 0 end), 0))
            from JobOfferEntity j left join j.applications a
            where j.company.id = :companyId and j.status = com.solvia.assistantrh.entity.enums.JobOfferStatus.OPEN
            group by j.id, j.title
            """,
            countQuery = """
            select count(j) from JobOfferEntity j
            where j.company.id = :companyId and j.status = com.solvia.assistantrh.entity.enums.JobOfferStatus.OPEN
            """)
    Page<JobOfferDashboardResponse> findOpenJobOfferDashboard(@Param("companyId") Long companyId, Pageable pageable);
}
