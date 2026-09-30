package com.solvia.assistantrh.entity;

import com.solvia.assistantrh.entity.base.BaseEntity;
import com.solvia.assistantrh.entity.enums.CandidateSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Personne qui postule. Ne porte aucun statut de recrutement : il est sur ApplicationEntity.
 * L'email est unique par entreprise ; sa normalisation (minuscules) est faite par le service.
 */
@Entity
@Table(
        name = "candidates",
        uniqueConstraints = @UniqueConstraint(name = "uk_candidates_company_email", columnNames = {"company_id", "email"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateEntity extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 30)
    private CandidateSource source;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_candidates_company"))
    private CompanyEntity company;

    @OneToMany(mappedBy = "candidate")
    @Builder.Default
    private List<ApplicationEntity> applications = new ArrayList<>();

    @OneToMany(mappedBy = "candidate")
    @Builder.Default
    private List<DocumentEntity> documents = new ArrayList<>();
}
