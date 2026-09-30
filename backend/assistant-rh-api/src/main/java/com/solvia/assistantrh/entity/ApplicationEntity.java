package com.solvia.assistantrh.entity;

import com.solvia.assistantrh.entity.base.BaseEntity;
import com.solvia.assistantrh.entity.enums.ApplicationStatus;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Candidature d'un candidat sur une offre. Porte le statut de suivi du recrutement.
 * <p>
 * appliedAt : date métier réelle de la candidature, distincte de createdAt, modifiable, jamais dans le futur.
 * statusChangedAt : date du dernier changement effectif de statut, distincte de updatedAt.
 * Les valeurs par défaut et la mise à jour de ces deux dates relèvent du service.
 */
@Entity
@Table(
        name = "applications",
        uniqueConstraints = @UniqueConstraint(name = "uk_applications_candidate_job_offer", columnNames = {"candidate_id", "job_offer_id"}),
        indexes = @Index(name = "idx_applications_job_offer", columnList = "job_offer_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.NEW;

    @PastOrPresent
    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @Column(name = "status_changed_at", nullable = false)
    private Instant statusChangedAt;

    /** Suppression du candidat : ses candidatures sont supprimées par la base. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_applications_candidate"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CandidateEntity candidate;

    /** Pas de cascade : une offre qui a des candidatures ne peut pas être supprimée (OFF-06). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_offer_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_applications_job_offer"))
    private JobOfferEntity jobOffer;

    @OneToMany(mappedBy = "application")
    @Builder.Default
    private List<InterviewEntity> interviews = new ArrayList<>();

    @OneToMany(mappedBy = "application")
    @Builder.Default
    private List<CommentEntity> comments = new ArrayList<>();
}
