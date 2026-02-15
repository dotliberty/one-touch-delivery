FROM maven:3.9.6-eclipse-temurin-21-alpine AS deps

WORKDIR /app

COPY pom.xml ./

RUN mvn -e dependency:go-offline -q


FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY --from=deps /root/.m2 /root/.m2

COPY pom.xml ./
COPY src/ ./src/

RUN mvn -e package -DskipTests -q


FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/notification-service-0.0.1-SNAPSHOT.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8086

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
    CMD wget --quiet --tries=1 --spider http://localhost:8086/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
