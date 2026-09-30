package com.solvia.assistantrh.entity;

import com.solvia.assistantrh.entity.base.BaseEntity;
import com.solvia.assistantrh.entity.enums.DocumentType;
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

/**
 * Fichier appartenant à un candidat. Métadonnées uniquement : le fichier est sur disque, aucun parsing en Phase 1.
 */
@Entity
@Table(name = "documents", indexes = @Index(name = "idx_documents_candidate", columnList = "candidate_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEntity extends BaseEntity {

    /** Nom d'origine du fichier, affiché à l'utilisateur. */
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    /** Chemin relatif au dossier d'upload, généré par le serveur. Jamais exposé par l'API. */
    @Column(name = "path", nullable = false, unique = true, length = 500)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private DocumentType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_documents_candidate"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CandidateEntity candidate;
}
