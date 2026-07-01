# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Cache dependencies before copying source (layer re-used on code-only changes)
COPY pom.xml .
COPY ingestion-service/pom.xml ingestion-service/pom.xml
COPY reconciliation-engine/pom.xml reconciliation-engine/pom.xml
COPY alert-service/pom.xml alert-service/pom.xml
COPY reporting-service/pom.xml reporting-service/pom.xml
RUN mvn dependency:go-offline -q -pl ingestion-service -am

COPY ingestion-service/src ingestion-service/src
RUN mvn package -DskipTests -q -pl ingestion-service -am

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /build/ingestion-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
