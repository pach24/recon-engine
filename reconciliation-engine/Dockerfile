# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
COPY ingestion-service/pom.xml ingestion-service/pom.xml
COPY reconciliation-engine/pom.xml reconciliation-engine/pom.xml
COPY alert-service/pom.xml alert-service/pom.xml
COPY reporting-service/pom.xml reporting-service/pom.xml
RUN mvn dependency:go-offline -q -pl reconciliation-engine -am

COPY reconciliation-engine/src reconciliation-engine/src
RUN mvn package -DskipTests -q -pl reconciliation-engine -am

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /build/reconciliation-engine/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
