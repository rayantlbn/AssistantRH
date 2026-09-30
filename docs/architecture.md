# Architecture technique

Ce document décrit comment le MVP est construit. Il s'appuie sur le [modèle métier](domain.md) et le [cahier des charges](cahier-des-charges.md), et sert de référence pour le Jour 4 (génération du backend) et le Jour 6 (frontend).

## 1. Vue globale

```
┌─────────────────────┐      HTTP / JSON       ┌──────────────────────────┐      JDBC      ┌──────────────┐
│  Frontend React     │ ─────────────────────▶ │  API REST Spring Boot    │ ─────────────▶ │  PostgreSQL  │
│  (navigateur)       │ ◀───────────────────── │  Java 21                 │ ◀───────────── │  17          │
└─────────────────────┘                        └────────────┬─────────────┘                └──────────────┘
                                                            │
                                                            ▼
                                                    uploads/ (fichiers CV)
```

- Le frontend est une application React qui ne parle qu'à l'API, en JSON. L'outil de build (Vite ou Next.js) sera choisi au Jour 6. Dans les deux cas, le frontend n'accède jamais directement à la base.
- L'API porte toutes les règles métier. Le frontend ne fait que de l'affichage et de la validation de confort (champs obligatoires, format email) ; le serveur revalide tout.
- PostgreSQL stocke les données. Les fichiers (CV) sont stockés sur disque, la base ne garde que leurs métadonnées.

En développement :

| Composant | Adresse |
|-----------|---------|
| Frontend | `http://localhost:5173` (Vite) ou `http://localhost:3000` (Next.js) |
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| PostgreSQL | `localhost:5432`, base `assistant_rh` |

En production, un reverse proxy Nginx (`infra/nginx/`) servira le frontend et redirigera `/api` vers le backend, ce qui évite les problèmes de CORS. Il n'est pas nécessaire pour le MVP en local.

## 2. Monorepo

```
assistant-rh-api-v2/
├── backend/
│   └── assistant-rh-api/        API Spring Boot (Maven, Java 21)
│       ├── src/
│       ├── pom.xml
│       ├── mvnw, .mvn/
│       └── Dockerfile
├── frontend/                    Application React (créée au Jour 6)
├── infra/
│   ├── docker-compose.yml       PostgreSQL + API
│   └── nginx/                   Reverse proxy (plus tard)
├── docs/                        Documentation produit et technique
├── pom.xml                      POM agrégateur (import IntelliJ, build depuis la racine)
├── CLAUDE.md
└── README.md
```

Règles :

- Chaque application reste autonome dans son dossier : le backend se construit avec son `mvnw`, le frontend avec `npm`. Aucun code partagé entre les deux.
- Le contrat entre frontend et backend est l'API REST documentée par OpenAPI. Le frontend pourra générer ses types à partir du fichier OpenAPI s'il le souhaite.
- Branches : `main` stable, `develop` pour l'intégration. Une branche par fonctionnalité si besoin, fusionnée dans `develop`.

## 3. Backend — structure des packages

Package racine : `com.solvia.assistantrh`.

```
com.solvia.assistantrh
├── AssistantRhApiV2Application.java
├── config/        Configuration Spring : CORS, OpenAPI, stockage des fichiers, entreprise par défaut
├── controller/    Contrôleurs REST : reçoivent les requêtes, valident, délèguent au service
├── dto/           Objets d'entrée (Request) et de sortie (Response) de l'API
├── entity/        Entités JPA et énumérations du domaine
├── exception/     Exceptions métier et GlobalExceptionHandler
├── mapper/        Mappers MapStruct entité ↔ DTO
├── repository/    Interfaces Spring Data JPA
└── service/       Règles métier et transactions
```

Le découpage se fait par couche, ce qui reste lisible avec sept entités. Si le projet grossit (au-delà d'une quinzaine d'entités, ou à l'arrivée des modules IA), on passera à un découpage par fonctionnalité (`candidate/`, `joboffer/`…).

Responsabilités et règles de dépendance :

| Couche | Fait | Ne fait pas |
|--------|------|-------------|
| controller | Lit la requête, valide le DTO (`@Valid`), appelle un service, renvoie le bon code HTTP | Aucune règle métier, aucun accès direct à un repository |
| service | Applique les règles métier de [domain.md](domain.md), gère les transactions (`@Transactional`), convertit via les mappers | Ne manipule pas d'objets HTTP |
| repository | Requêtes en base | Aucune logique |
| mapper | Conversion entité ↔ DTO | Aucune logique métier, aucun appel à un repository |
| entity | Représente les tables | N'est jamais renvoyée par l'API |

Les dépendances vont toujours dans le même sens : `controller → service → repository`. Un service peut appeler un autre service.

Les services renvoient des DTO, pas des entités. Comme `spring.jpa.open-in-view=false`, toute conversion qui touche une relation paresseuse doit se faire à l'intérieur de la transaction du service.

Nommage des classes :

| Élément | Exemple |
|---------|---------|
| Entité | `CandidateEntity` (table `candidates`) |
| Énumération | `ApplicationStatus`, `CandidateSource` |
| Repository | `CandidateRepository` |
| Service | `CandidateService` |
| Controller | `CandidateController` |
| DTO | `CandidateRequest`, `CandidateResponse` |
| Mapper | `CandidateMapper` |

Le suffixe `Entity` évite les collisions avec des noms courants (`Document` existe dans `org.w3c.dom`, `Application` prête à confusion avec la classe principale de Spring Boot). Les noms métier de [domain.md](domain.md) restent Candidate, JobOffer, Application, etc.

Relations JPA :

- Toutes les relations sont des `@ManyToOne(fetch = LAZY)` du côté enfant (ex. `ApplicationEntity.candidate`). Pas de collection `@OneToMany` par défaut : les listes s'obtiennent par requête (`findByCandidateId`).
- Les suppressions en cascade décrites dans [domain.md](domain.md) sont portées par les clés étrangères (`@OnDelete(action = CASCADE)`), pour que la base reste cohérente même en cas de suppression directe.
- Les fichiers sur disque ne sont pas concernés par la cascade SQL : le service qui supprime un candidat ou un document supprime aussi les fichiers, une fois la transaction validée.

L'entreprise : en Phase 1 il n'y en a qu'une. Au démarrage, un initialiseur dans `config/` la crée si la table est vide (nom lu dans `app.company.name`). Les services obtiennent l'entreprise courante par un composant unique, `CurrentCompanyProvider`, et ne la cherchent jamais eux-mêmes. C'est ce composant qui changera à l'arrivée de la connexion (voir section 10).

Dépendances Maven à ajouter au Jour 4 : MapStruct (avec `lombok-mapstruct-binding`, pour que Lombok et MapStruct fonctionnent ensemble) et springdoc-openapi (Swagger UI, exigence QUA-01). Rien d'autre.

## 4. Conventions REST

Règles générales :

- Toutes les routes commencent par `/api`. Ressources au pluriel, en kebab-case (`/api/job-offers`).
- JSON en entrée et en sortie, sauf l'upload et le téléchargement de fichiers.
- Dates au format ISO 8601 en UTC (`2026-10-01T09:30:00Z`).
- Énumérations transmises avec leur valeur de code (`"status": "SHORTLISTED"`). La traduction est faite par le frontend.
- Codes de retour : `200` lecture et modification, `201` création (avec en-tête `Location`), `204` suppression, erreurs selon la section 5.
- `PUT` remplace toutes les données modifiables de la ressource. Les champs non modifiables (liens vers le candidat ou l'offre, dates de création) sont ignorés s'ils sont envoyés.

Pagination (exigence PERF-02) : toutes les listes sont paginées avec les paramètres `page` (à partir de 0), `size` (20 par défaut, 100 maximum) et `sort` (ex. `sort=createdAt,desc`). Réponse :

```json
{
  "content": [ ... ],
  "page": { "number": 0, "size": 20, "totalElements": 57, "totalPages": 3 }
}
```

L'entreprise n'a pas d'endpoint en Phase 1 : elle est implicite.

### Candidates

| Méthode | Route | Rôle | Réponse |
|---------|-------|------|---------|
| GET | `/api/candidates?search=` | Liste paginée, du plus récent au plus ancien. `search` filtre sur nom, prénom ou email (SRC-01) | 200 |
| GET | `/api/candidates/{id}` | Détail | 200, 404 |
| POST | `/api/candidates` | Création | 201, 400, 409 |
| PUT | `/api/candidates/{id}` | Modification | 200, 400, 404, 409 |
| DELETE | `/api/candidates/{id}` | Suppression avec candidatures, entretiens, commentaires et documents | 204, 404 |

`CandidateRequest` : `firstName`, `lastName`, `email` (obligatoires), `phone`, `source`, `notes`.
`CandidateResponse` : tous les attributs, plus `createdAt` et `updatedAt`.

### Job offers

| Méthode | Route | Rôle | Réponse |
|---------|-------|------|---------|
| GET | `/api/job-offers?status=` | Liste paginée, filtrable par statut (OFF-05) | 200 |
| GET | `/api/job-offers/{id}` | Détail | 200, 404 |
| POST | `/api/job-offers` | Création, toujours au statut `DRAFT` | 201, 400 |
| PUT | `/api/job-offers/{id}` | Modification, y compris le changement de statut | 200, 400, 404, 409 |
| DELETE | `/api/job-offers/{id}` | Suppression, refusée si l'offre a des candidatures (OFF-06) | 204, 404, 409 |

`JobOfferRequest` : `title` (obligatoire), `description`, `location`, `contractType`, `status` (ignoré à la création).
Changement de statut : seules les transitions de [domain.md](domain.md#joboffer) sont acceptées, sinon 409. Une offre `CLOSED` ne peut que repasser en `OPEN` ; toute autre modification est refusée avec un 409 (OFF-03).

### Applications

| Méthode | Route | Rôle | Réponse |
|---------|-------|------|---------|
| GET | `/api/applications?jobOfferId=&candidateId=&status=` | Liste paginée et filtrée. Sert aussi aux fiches offre et candidat (DSH-02, DSH-03, SRC-02) | 200 |
| GET | `/api/applications/{id}` | Détail | 200, 404 |
| POST | `/api/applications` | Crée une candidature au statut `NEW` | 201, 400, 404, 409 |
| PUT | `/api/applications/{id}` | Change le statut ou corrige la date de candidature | 200, 400, 404 |
| DELETE | `/api/applications/{id}` | Suppression avec entretiens et commentaires | 204, 404 |

`ApplicationCreateRequest` : `candidateId`, `jobOfferId` (obligatoires), `appliedAt` (facultatif, date de création par défaut). Un id qui n'existe pas renvoie 404.
`ApplicationUpdateRequest` : `status`, `appliedAt` (obligatoires). Le candidat et l'offre d'une candidature ne changent jamais.
Un `appliedAt` dans le futur renvoie un 400 (APP-08).
`ApplicationResponse` : `id`, `status`, `appliedAt`, `statusChangedAt`, `createdAt`, `updatedAt`, un résumé du candidat (`id`, `firstName`, `lastName`) et de l'offre (`id`, `title`), pour éviter au frontend un appel par ligne.

### Tableau de bord

| Méthode | Route | Rôle | Réponse |
|---------|-------|------|---------|
| GET | `/api/dashboard/job-offers` | Offres `OPEN` avec le nombre de candidatures par statut (DSH-01) | 200 |

Ce n'est pas une ressource CRUD mais une vue de lecture. Les comptages sont calculés en une seule requête SQL groupée, pas en chargeant les candidatures.

### Interviews

| Méthode | Route | Rôle | Réponse |
|---------|-------|------|---------|
| GET | `/api/interviews?applicationId=` | Liste paginée, triée par date | 200 |
| GET | `/api/interviews/{id}` | Détail | 200, 404 |
| POST | `/api/interviews` | Planification | 201, 400, 404, 409 |
| PUT | `/api/interviews/{id}` | Modification, saisie du compte rendu et de l'avis | 200, 400, 404 |
| DELETE | `/api/interviews/{id}` | Suppression | 204, 404 |

`InterviewRequest` : `applicationId` (création uniquement), `date`, `type` (obligatoires), `participants`, `feedback`, `outcome`.
Les règles sur `outcome` (vide tant que la date est future, obligatoire dès qu'un `feedback` est saisi) renvoient un 400. Créer un entretien sur une candidature `HIRED` ou `REJECTED` renvoie un 409.

### Comments

| Méthode | Route | Rôle | Réponse |
|---------|-------|------|---------|
| GET | `/api/applications/{applicationId}/comments` | Liste paginée, du plus récent au plus ancien | 200, 404 |
| POST | `/api/applications/{applicationId}/comments` | Ajout | 201, 400, 404 |
| DELETE | `/api/comments/{id}` | Suppression | 204, 404 |

`CommentRequest` : `content` (obligatoire).
Pas de `PUT` : un commentaire ne se modifie pas, on le supprime et on en écrit un autre ([domain.md](domain.md#comment)). Pas d'auteur en Phase 1.

### Documents

Les fichiers ne suivent pas le CRUD générique : un document se crée par upload et ne se modifie pas.

| Méthode | Route | Rôle | Réponse |
|---------|-------|------|---------|
| GET | `/api/candidates/{candidateId}/documents` | Liste des documents d'un candidat (métadonnées) | 200, 404 |
| POST | `/api/candidates/{candidateId}/documents` | Upload, en `multipart/form-data` | 201, 400, 404 |
| GET | `/api/documents/{id}` | Téléchargement du fichier | 200, 404 |
| DELETE | `/api/documents/{id}` | Suppression de l'entité et du fichier | 204, 404 |

Upload : deux parties, `file` (obligatoire) et `type` (`CV` par défaut). Le serveur vérifie que le fichier est un PDF (type annoncé et signature `%PDF` en début de fichier) et qu'il fait au plus 5 Mo. Sinon, 400.

Téléchargement : réponse binaire avec `Content-Type: application/pdf` et `Content-Disposition: attachment; filename="<filename>"`.

`DocumentResponse` : `id`, `filename`, `type`, `createdAt`. Le chemin de stockage (`path`) n'est jamais exposé.

Aucun OCR ni parsing en Phase 1.

## 5. Gestion des erreurs

Toutes les erreurs sont traitées à un seul endroit : `GlobalExceptionHandler` (`@RestControllerAdvice`, package `exception/`). Les controllers ne font jamais de `try/catch` pour construire une réponse d'erreur.

Format de réponse : le standard Problem Details (RFC 9457), pris en charge nativement par Spring (`ProblemDetail`), complété d'un code métier stable que le frontend peut utiliser.

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Ce candidat a déjà une candidature pour cette offre.",
  "instance": "/api/applications",
  "code": "DUPLICATE_APPLICATION"
}
```

Pour les erreurs de validation, un champ `errors` liste les champs en faute :

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "La requête contient des champs invalides.",
  "code": "VALIDATION_ERROR",
  "errors": [ { "field": "email", "message": "doit être une adresse email valide" } ]
}
```

Correspondance :

| HTTP | Quand | Exceptions |
|------|-------|------------|
| 400 BAD_REQUEST | Données invalides : champ manquant, mauvais format, fichier refusé, règle sur l'avis d'entretien | `MethodArgumentNotValidException`, `InvalidRequestException`, `InvalidDocumentException`, `MaxUploadSizeExceededException` |
| 404 NOT_FOUND | Ressource introuvable, dans l'URL ou référencée dans le corps | `ResourceNotFoundException` |
| 409 CONFLICT | La requête est valide mais contredit une règle métier ou l'état actuel des données | `DuplicateApplicationException`, `DuplicateCandidateEmailException`, `InvalidStatusTransitionException`, `JobOfferNotOpenException`, `JobOfferHasApplicationsException`, `InterviewNotAllowedException` |
| 500 INTERNAL_SERVER_ERROR | Erreur imprévue | toute autre exception |

Exemple de 409 : `DuplicateApplicationException`

- Cas métier : on tente de créer une candidature pour un candidat qui en a déjà une sur la même offre, quel que soit son statut.
- Référence : APP-02, et la contrainte d'unicité `(candidate_id, job_offer_id)` de [domain.md](domain.md#application).
- Le service vérifie l'existence avant d'insérer et lève `DuplicateApplicationException`. Si deux requêtes arrivent en même temps, c'est la contrainte d'unicité en base qui bloque : la `DataIntegrityViolationException` correspondante est convertie en la même réponse 409.

Autres règles :

- Les exceptions métier héritent d'une classe commune `BusinessException` qui porte le code (`DUPLICATE_APPLICATION`…) et le statut HTTP. Le handler n'a donc qu'une méthode pour toutes.
- Une erreur 500 renvoie un message générique. La trace complète est écrite dans les logs, jamais dans la réponse (SEC-05).
- Les messages `detail` sont en français, les `code` en anglais et stables dans le temps.

## 6. DTO

Chaque entité a trois représentations distinctes dès le départ :

| Classe | Package | Rôle |
|--------|---------|------|
| `CandidateEntity` | `entity` | Correspond à la table. Ne sort jamais du backend |
| `CandidateRequest` | `dto` | Ce que le client envoie. Porte les annotations de validation |
| `CandidateResponse` | `dto` | Ce que l'API renvoie |

Même logique pour toutes les entités. Quand la création et la modification n'acceptent pas les mêmes champs, on sépare la requête en deux (`ApplicationCreateRequest` / `ApplicationUpdateRequest`).

Pourquoi :

- On peut modifier une table sans casser l'API, et inversement.
- Le client ne peut pas envoyer de champs qu'il ne devrait pas contrôler (`id`, `createdAt`, `statusChangedAt`, `path`).
- On évite les boucles infinies de sérialisation et le chargement accidentel de relations.

Les DTO sont des `record` Java : immuables, sans Lombok. La validation utilise Jakarta Validation (`@NotBlank`, `@Email`, `@Size`, `@NotNull`) avec les longueurs de [domain.md](domain.md). Les règles qui dépendent de la base (email en double, statut de l'offre) sont dans les services, pas dans les annotations.

Les entités utilisent Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`) mais jamais `@Data`, dont le `equals`/`hashCode` pose problème avec JPA.

## 7. Mapper

Les conversions entité ↔ DTO sont faites par MapStruct, un mapper par entité (`CandidateMapper`, `JobOfferMapper`…), déclaré avec `@Mapper(componentModel = "spring")`.

```java
@Mapper(componentModel = "spring")
public interface CandidateMapper {
    CandidateResponse toResponse(CandidateEntity entity);
    CandidateEntity toEntity(CandidateRequest request);
    void updateEntity(CandidateRequest request, @MappingTarget CandidateEntity entity);
}
```

MapStruct génère le code à la compilation : pas de réflexion, et une erreur de compilation si un champ n'est pas mappé. On évite ainsi des `.builder()` recopiés dans chaque service.

Règles :

- Les champs gérés par le serveur (`id`, `createdAt`, `updatedAt`, `status` initial, `statusChangedAt`, `company`, `path`) sont ignorés explicitement dans les mappers (`@Mapping(target = ..., ignore = true)`). Le service les renseigne.
- Les mappers ne font aucun accès en base. Pour créer une candidature, le service charge le candidat et l'offre, puis les affecte à l'entité.
- Politique `unmappedTargetPolicy = ReportingPolicy.ERROR` : un nouveau champ oublié dans un mapper fait échouer la compilation.

## 8. Persistance des CV

Pour le MVP, les fichiers sont stockés sur le disque local, dans un dossier `uploads/`. Pas de S3, pas de MinIO, pas de cloud.

- Emplacement configurable : `app.storage.upload-dir`, valeur par défaut `uploads` (relatif au dossier de lancement), surchargeable par la variable `UPLOAD_DIR`.
- Organisation : `uploads/candidates/{candidateId}/{uuid}.pdf`. Le nom sur disque est généré par le serveur. Le nom d'origine est conservé dans `Document.filename`, uniquement pour l'affichage.
- `Document.path` contient le chemin relatif au dossier d'upload (`candidates/12/3f9c…pdf`), pour pouvoir déplacer le dossier sans modifier la base.
- Taille limitée à 5 Mo par la configuration Spring (`spring.servlet.multipart.max-file-size`) et revérifiée par le service.
- Le dossier `uploads/` est ignoré par Git. Il contient des données personnelles : on n'y met jamais de vrais CV en développement (RGPD-04).

Tout accès au disque passe par une interface `DocumentStorage` (`store`, `load`, `delete`), avec une implémentation `LocalDocumentStorage`. Le reste du code ignore où sont les fichiers. Quand il faudra passer à un stockage objet (roadmap, Phase 2), on écrira une deuxième implémentation, sans toucher aux services ni aux controllers.

Ordre des opérations à l'upload : écrire le fichier, puis enregistrer l'entité. Si l'enregistrement échoue, le fichier est supprimé. À la suppression : supprimer l'entité, puis le fichier après validation de la transaction. Un fichier orphelin est acceptable ; une entité qui pointe vers un fichier absent ne l'est pas.

## 9. Docker

Déjà en place dans `infra/docker-compose.yml` :

- `postgres` : PostgreSQL 17, volume `postgres-data` pour les données.
- `api` : le backend, construit depuis `backend/assistant-rh-api/Dockerfile`, activé avec le profil `full`.

Usage :

| Besoin | Commande |
|--------|----------|
| Développement (backend lancé avec Maven) | `docker compose -f infra/docker-compose.yml up -d`, puis `./mvnw spring-boot:run` |
| Tout dans Docker | `docker compose -f infra/docker-compose.yml --profile full up -d --build` |

Le backend lit sa configuration dans des variables d'environnement (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SERVER_PORT`, et `UPLOAD_DIR` à venir). Aucun secret n'est écrit dans le code (SEC-04). Les identifiants PostgreSQL du compose sont des valeurs de développement.

Stockage des CV dans Docker. En l'état, deux problèmes empêcheraient l'upload de fonctionner dans le conteneur :

- le conteneur tourne avec l'utilisateur non-root `spring`, qui ne peut pas écrire dans `/app` (propriété de root), donc ne peut pas créer `uploads/` ;
- aucun volume n'est prévu : les fichiers seraient perdus à chaque recréation du conteneur.

Corrections à appliquer en même temps que l'implémentation de l'upload.

Dans `backend/assistant-rh-api/Dockerfile`, étape runtime, créer le dossier et le donner à `spring` avant de changer d'utilisateur :

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 spring \
    && mkdir -p /app/uploads \
    && chown spring /app/uploads
USER spring
```

Dans `infra/docker-compose.yml`, service `api` : fixer l'emplacement et monter un volume nommé.

```yaml
  api:
    environment:
      UPLOAD_DIR: /app/uploads
    volumes:
      - uploads-data:/app/uploads

volumes:
  postgres-data:
  uploads-data:
```

Un volume nommé créé par Docker reprend le propriétaire du dossier de l'image (`spring`), l'écriture fonctionne donc sans autre réglage. En développement hors Docker, `uploads/` est créé dans `backend/assistant-rh-api/` et ignoré par Git.

Le frontend aura son propre `Dockerfile` et un service dans le même compose au Jour 6.

Schéma de base : `spring.jpa.hibernate.ddl-auto=update` pendant la Phase 1, pour avancer vite. Avant la première mise en production, il sera remplacé par des migrations versionnées (QUA-04).

## 10. Sécurité

Pas d'authentification en Phase 1 : la connexion est un Should Have (S1). L'API est ouverte et ne doit tourner qu'en local ou en démonstration, avec des données fictives.

Ce qui est déjà en place ou prévu dès la Phase 1 :

- Validation de toutes les entrées côté serveur (SEC-03).
- Aucune trace technique dans les réponses d'erreur (SEC-05).
- Secrets en variables d'environnement (SEC-04).
- CORS : une configuration dans `config/` autorise uniquement l'adresse du frontend de développement.
- Contrôle du type et de la taille des fichiers uploadés.

Où Spring Security s'insérera (à ne pas implémenter maintenant) :

```
Requête HTTP
    │
    ▼
SecurityFilterChain            ← config/SecurityConfig : authentification, routes publiques / protégées
    │
    ▼
Controller                     ← inchangé
    │
    ▼
Service ── CurrentCompanyProvider   ← renvoie l'entreprise de l'utilisateur connecté au lieu de l'entreprise unique
    │
    ▼
Repository                     ← requêtes déjà filtrées par company_id
```

Ce qui changera :

1. Ajout de `spring-boot-starter-security` et d'une classe `SecurityConfig` dans `config/`.
2. Nouvelles entités `UserEntity` et `RoleEntity`, rattachées à Company ([domain.md](domain.md#points-dextension-futurs)).
3. `CurrentCompanyProvider` lit l'entreprise dans l'utilisateur authentifié au lieu de renvoyer l'entreprise par défaut.
4. Ajout de l'auteur sur les commentaires et, si besoin, des participants liés à des utilisateurs sur les entretiens.

Ce qui ne changera pas : les controllers, les DTO existants et les règles métier. C'est la raison pour laquelle, dès la Phase 1, les services passent par `CurrentCompanyProvider` et filtrent candidats et offres par `company_id`, même s'il n'y a qu'une entreprise.

Le mode d'authentification (session ou jeton JWT) sera choisi au moment de S1, en fonction de la manière dont le frontend est servi.

## 11. Extension IA future

Architecture cible, à titre indicatif :

```
Frontend
    │
    ▼
Spring Boot API
    │
    ├── Core Business Modules       (Phase 1 : candidats, offres, candidatures, entretiens, documents, commentaires)
    │
    └── AI Modules                  (Phases 3 à 5)
          ├── OcrService
          ├── CvParserService
          ├── MatchingService
          └── CandidateScoringService
                    │
                    ▼
              OpenAI / Claude
```

Les modules IA n'existent pas en Phase 1. Aucune classe, aucune dépendance, aucune configuration liée à l'IA n'est ajoutée maintenant.

L'architecture doit permettre de les ajouter sans refonte majeure :

- Ils vivront dans un package dédié (`com.solvia.assistantrh.ai`) et s'appuieront sur les services métier existants, jamais l'inverse. Le cœur métier ne dépend pas des modules IA.
- Ils se brancheront sur les entités prévues dans [domain.md](domain.md#points-dextension-futurs) : OCR et parsing sur Document, matching et scoring sur Application, embeddings sur Candidate, JobOffer et Document, assistant conversationnel filtré par Company.
- Leurs résultats seront stockés dans des tables à eux (texte extrait, score, explication), sans modifier les tables existantes.
- Les traitements longs (OCR, parsing) seront asynchrones : l'upload d'un document reste instantané, l'analyse arrive ensuite.

Aucun fournisseur IA n'est couplé au cœur métier :

- Chaque service IA est défini par une interface (ex. `CvParserService`). Les appels à un fournisseur (OpenAI, Claude, ou un modèle local) sont dans des implémentations séparées, choisies par configuration.
- Le SDK d'un fournisseur n'est importé que dans son implémentation. Changer de fournisseur ne touche ni les services métier, ni les controllers, ni la base.
- L'IA propose, l'utilisateur valide : aucun résultat IA n'écrit directement dans une fiche candidat ou ne change un statut sans action de l'utilisateur (roadmap, phases 3 et 4).
- Avant d'envoyer des données personnelles à un fournisseur externe, une validation juridique est nécessaire (loi 09-08, RGPD).

## 12. Timestamps techniques

Toutes les entités ont deux dates techniques :

| Attribut | Colonne | Renseigné |
|----------|---------|-----------|
| `createdAt` | `created_at TIMESTAMPTZ NOT NULL` | à l'insertion, jamais modifié ensuite |
| `updatedAt` | `updated_at TIMESTAMPTZ NOT NULL` | à l'insertion, puis à chaque modification de la ligne |

Elles sont portées par une classe commune `BaseEntity` (`@MappedSuperclass`) dont héritent toutes les entités, avec l'id. Les valeurs sont posées automatiquement par Hibernate (`@CreationTimestamp`, `@UpdateTimestamp`) : ni le service ni le client ne les renseignent.

Ne pas confondre avec les dates métier de [domain.md](domain.md) :

| Champ | Nature | Mis à jour quand |
|-------|--------|------------------|
| `updatedAt` (toutes les entités) | Technique | N'importe quelle modification de la ligne, quel que soit le champ |
| `statusChangedAt` (Application) | Métier | Uniquement quand le statut change réellement (APP-07). Réenregistrer le même statut ne le modifie pas |
| `appliedAt` (Application) | Métier | Date réelle de la candidature. Vaut la date de création par défaut et peut être corrigée (APP-08) |
| `date` (Interview) | Métier | Date de l'entretien, choisie par l'utilisateur |

Exemples :

- On passe une candidature de `NEW` à `SHORTLISTED` : `updatedAt` et `statusChangedAt` changent tous les deux.
- On renvoie ensuite `SHORTLISTED` sans changement : aucune des deux dates ne bouge, la ligne n'est pas modifiée.
- On corrige `appliedAt` d'une candidature saisie en retard : `updatedAt` change, `statusChangedAt` non.
- Une candidature saisie aujourd'hui pour un CV reçu il y a une semaine a `appliedAt` = il y a une semaine, `createdAt` = aujourd'hui. Les tris et les délais de recrutement utilisent `appliedAt`.

`updatedAt` et `statusChangedAt` restent deux colonnes distinctes et ne doivent jamais être fusionnées : le premier sert au suivi technique, le second au suivi du recrutement et au tableau de bord.
