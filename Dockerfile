# NOTE: User requested Java 25; adjust base image to match your environment if Java 25 images are available.
ARG JAVA_IMAGE=eclipse-temurin:25-jre
FROM ${JAVA_IMAGE}
WORKDIR /app
COPY target/*-runner.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
