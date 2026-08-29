# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

ENV SERVER_PORT=8080

RUN apk add --no-cache curl && \
    addgroup -S spring && adduser -S spring -G spring

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:$SERVER_PORT/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
