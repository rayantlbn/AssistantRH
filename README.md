# Assistant RH

Monorepo de l'Assistant RH Solvia.

```
.
├── backend/assistant-rh-api/   # API Spring Boot (Java 21, Maven)
├── frontend/                   # Application web (à venir)
├── infra/
│   ├── docker-compose.yml      # PostgreSQL + API conteneurisée
│   └── nginx/                  # Reverse proxy (à venir)
├── docs/                       # Documentation
└── pom.xml                     # POM agrégateur (import IntelliJ / build depuis la racine)
```

## Prérequis

- JDK 21
- Docker

## Démarrage en développement

```bash
# 1. Base de données
docker compose -f infra/docker-compose.yml up -d

# 2. Backend
cd backend/assistant-rh-api
./mvnw spring-boot:run
```

L'API écoute sur http://localhost:8080.

## Tout lancer dans Docker

```bash
docker compose -f infra/docker-compose.yml --profile full up -d --build
```

## Configuration du backend

| Variable      | Défaut                                          |
|---------------|-------------------------------------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/assistant_rh` |
| `DB_USERNAME` | `assistant_rh`                                  |
| `DB_PASSWORD` | `assistant_rh`                                  |
| `SERVER_PORT` | `8080`                                          |

## Branches

- `main` : version stable
- `develop` : intégration des développements en cours
