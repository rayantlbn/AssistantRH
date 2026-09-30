# Assistant RH — Contexte projet (mémoire persistante)

> Ce fichier sert de mémoire de travail pour le projet SaaS "Assistant RH".
> Ne rien coder tant que l'utilisateur ne le demande pas explicitement.
> Mission en attente : produire 4 documents (voir "Mission à réaliser" en bas), rien d'autre pour l'instant.

## Stack actuelle

- Backend : Spring Boot 4.1.1
- Java 21
- PostgreSQL
- Maven
- Docker
- Git (branches `main` et `develop`)
- Monorepo :
```
assistant-rh-api-v2/
├── backend/
│   └── assistant-rh-api/
├── frontend/
├── infra/
├── docs/
└── pom.xml
```

Objectif général : construire progressivement un produit commercialisable. Formaliser le MVP avant de développer davantage.

## Mission à réaliser (dès que l'utilisateur donne le go)

Produire, dans `docs/` :
1. `docs/mvp.md`
2. `docs/cahier-des-charges.md`
3. `docs/architecture.md`
4. `docs/roadmap.md`

Documents très détaillés, structurés, exploitables directement dans le projet.

## Vision produit

Assistant RH intelligent pour PME.

Problème actuel :
- CV dispersés, reçus par email
- Candidatures éparpillées
- Recruteurs perdent du temps à trier
- Suivi des entretiens sous Excel
- Infos candidats difficiles à retrouver
- PME n'ont pas les moyens d'un ATS complexe

Objectif : ATS simple enrichi par l'IA. **L'IA ne doit pas être au centre du MVP** — elle arrive progressivement. Le MVP doit d'abord résoudre un vrai problème métier.

## Utilisateurs cibles — à analyser et comparer

1. PME
2. Cabinet de recrutement
3. Recruteur indépendant
4. Startup

Recommander le meilleur segment pour le MVP, justifié par : facilité d'acquisition, valeur métier, simplicité technique, potentiel commercial.

## MVP — à définir précisément

- Quel problème est résolu ?
- Pour qui ?
- Quelle proposition de valeur ?
- Pourquoi un client paierait ?

Fonctionnalités classées en Must Have / Should Have / Could Have.

## Entités métier — à proposer

Exemples de départ : User, Role, Candidate, JobOffer, Interview, Application, Document, Comment, Company.

Pour chaque entité : description, attributs principaux, relations.

## Modèle de données

Diagramme Mermaid ERD cohérent avec le MVP, du type :
```
erDiagram
    USER ||--o{ CANDIDATE : manages
    JOB_OFFER ||--o{ APPLICATION : receives
```

## API REST — à définir

Exemples : `/api/candidates`, `/api/job-offers`, `/api/interviews`, `/api/applications`.

Pour chaque endpoint : méthode HTTP, description, payload principal.

## Roadmap — 5 phases

- Phase 1 : MVP ATS simple
- Phase 2 : Gestion documentaire
- Phase 3 : IA de parsing CV
- Phase 4 : Matching candidat / offre
- Phase 5 : Assistant RH IA complet

Pour chaque phase : objectifs, fonctionnalités, complexité, valeur métier.

## Contraintes générales

- Rester réaliste, éviter le sur-engineering
- Penser comme un fondateur SaaS
- Prioriser la rapidité de mise sur le marché
- Pas de fonctionnalités inutiles
- Architecture simple mais évolutive
- Les documents doivent servir de référence pour les prochains mois de développement

## IA & traitement documentaire (à anticiper dans l'architecture, pas forcément dans le MVP commercial)

À analyser :
1. OCR des CV PDF
2. Parsing automatique de CV
3. Extraction structurée : nom, prénom, email, téléphone, compétences, expériences, formations, certifications, langues
4. Génération de données de test : CV synthétiques, jeux de données candidats, offres simulées, scénarios de recrutement
5. Matching : score candidat ↔ offre, explication du score, critères utilisés
6. Assistant conversationnel RH

Pour chaque brique, préciser :
- ce qui appartient au MVP / V2 / V3
- quelles briques peuvent être développées dès maintenant
- quelles nécessitent des données réelles
- quelles nécessitent un modèle IA
- quelles peuvent être simulées au départ

Modules IA proposés à évaluer : `DocumentService`, `OcrService`, `CvParserService`, `MatchingService`, `AiService`.

## Dataset & données — stratégie à proposer

Sources possibles : CV générés automatiquement, CV anonymisés, données publiques, données synthétiques générées par IA.

À définir : volume cible, structure des données, format de stockage, stratégie d'anonymisation.

Objectif : pouvoir entraîner, tester et évaluer les fonctionnalités IA sans dépendre immédiatement de vrais clients.

## Notes de continuité

- Ce projet ("Assistant RH" SaaS commercialisable) est distinct du projet démonstrateur "Assistant RH IA" (4 jours solo, cahier des charges déjà livré séparément pour SOLVIA) — ne pas confondre les deux lors des prochaines sessions.
- RGPD : toujours utiliser des données fictives/synthétiques pour les tests, jamais de données personnelles réelles.
