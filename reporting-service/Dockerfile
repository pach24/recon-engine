# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
COPY ingestion-service/pom.xml ingestion-service/pom.xml
COPY reconciliation-engine/pom.xml reconciliation-engine/pom.xml
COPY alert-service/pom.xml alert-service/pom.xml
COPY reporting-service/pom.xml reporting-service/pom.xml
RUN mvn dependency:go-offline -q -pl reporting-service -am

COPY reporting-service/src reporting-service/src
RUN mvn package -DskipTests -q -pl reporting-service -am

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /build/reporting-service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
