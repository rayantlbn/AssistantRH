package com.solvia.assistantrh.entity;

import com.solvia.assistantrh.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entreprise cliente. Une seule en Phase 1, sans logique multi-entreprise.
 */
@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @OneToMany(mappedBy = "company")
    @Builder.Default
    private List<CandidateEntity> candidates = new ArrayList<>();

    @OneToMany(mappedBy = "company")
    @Builder.Default
    private List<JobOfferEntity> jobOffers = new ArrayList<>();
}
