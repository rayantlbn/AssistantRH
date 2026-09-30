package com.solvia.assistantrh.entity;

import com.solvia.assistantrh.entity.base.BaseEntity;
import com.solvia.assistantrh.entity.enums.InterviewOutcome;
import com.solvia.assistantrh.entity.enums.InterviewType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Entretien rattaché à une candidature. outcome (avis catégorisé) et feedback (compte rendu libre) sont indépendants.
 */
@Entity
@Table(name = "interviews", indexes = @Index(name = "idx_interviews_application", columnList = "application_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewEntity extends BaseEntity {

    @Column(name = "date", nullable = false)
    private Instant date;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private InterviewType type;

    /** Noms des participants côté entreprise, séparés par des virgules. Pas de lien vers des utilisateurs en Phase 1. */
    @Column(name = "participants", length = 500)
    private String participants;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 20)
    private InterviewOutcome outcome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_interviews_application"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ApplicationEntity application;
}
