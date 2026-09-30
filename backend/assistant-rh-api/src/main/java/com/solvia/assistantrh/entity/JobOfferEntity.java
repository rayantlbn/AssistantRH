package com.solvia.assistantrh.entity;

import com.solvia.assistantrh.entity.base.BaseEntity;
import com.solvia.assistantrh.entity.enums.ContractType;
import com.solvia.assistantrh.entity.enums.JobOfferStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Poste à pourvoir. Créé au statut DRAFT.
 */
@Entity
@Table(name = "job_offers", indexes = @Index(name = "idx_job_offers_company", columnList = "company_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOfferEntity extends BaseEntity {

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", length = 150)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 20)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private JobOfferStatus status = JobOfferStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_job_offers_company"))
    private CompanyEntity company;

    @OneToMany(mappedBy = "jobOffer")
    @Builder.Default
    private List<ApplicationEntity> applications = new ArrayList<>();
}
