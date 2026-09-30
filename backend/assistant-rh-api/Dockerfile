# --- Build ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# --- Runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app
# Utilisateur non-root, propriétaire du dossier des CV (seul dossier où l'application écrit)
RUN useradd --system --uid 1001 spring \
    && mkdir -p /app/uploads \
    && chown spring /app/uploads
USER spring
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
