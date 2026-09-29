# assistant-rh-api-v2

API Spring Boot (Java 21, Maven) de l'Assistant RH Solvia.

## Prérequis

- JDK 21 (`JAVA_HOME` doit pointer sur un JDK 21)
- Docker (pour PostgreSQL)

## Démarrage

```bash
docker compose up -d
./mvnw spring-boot:run
```

L'API écoute sur http://localhost:8080.

## Configuration

| Variable      | Défaut                                          |
|---------------|-------------------------------------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/assistant_rh` |
| `DB_USERNAME` | `assistant_rh`                                  |
| `DB_PASSWORD` | `assistant_rh`                                  |
| `SERVER_PORT` | `8080`                                          |
