# Sahamati Token Service (Quarkus)

This service provides an endpoint to generate and store Entity Member tokens (Sahama­ti-style). It uses PostgreSQL for persistence (Hibernate ORM / Panache).

Key features:
- POST /api/v1/member/{memberId}/token -> returns an existing valid token or generates a new token and persists it
- Config table (app_config) to store endpoints / credentials / secrets
- Helper methods for user token generation, entity secret read/reset (service-level)
- Global exception handling
- Dockerfile and Kubernetes manifests included

Build:
  mvn -DskipTests package

Run locally (requires Postgres):
  export DB_URL=jdbc:postgresql://localhost:5432/sahamati_db
  export DB_USER=postgres
  export DB_PASSWORD=postgres
  mvn -DskipTests quarkus:dev
  or
  java -jar target/*-runner.jar

Docker:
  mvn -DskipTests package
  docker build -t your-registry/sahamati-token-service:latest .

Kubernetes:
  kubectl apply -f k8s/

Notes:
- Adjust application.properties and k8s configmap/secret with real values.
- The external API call payload/response parsing may need adjustments to match the real Saafe endpoints; adapt obtainAndPersistEntityToken accordingly.
