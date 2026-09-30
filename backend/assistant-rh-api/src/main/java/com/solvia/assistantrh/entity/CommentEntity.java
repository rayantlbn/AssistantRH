package com.solvia.assistantrh.entity;

import com.solvia.assistantrh.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Note interne sur une candidature. Pas d'auteur en Phase 1 (pas d'utilisateurs).
 */
@Entity
@Table(name = "comments", indexes = @Index(name = "idx_comments_application", columnList = "application_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentEntity extends BaseEntity {

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_comments_application"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ApplicationEntity application;
}
