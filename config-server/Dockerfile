# STAGE 1: BUILD #
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:resolve -q

COPY src/ ./src/
RUN mvn package -DskipTests -q

# STAGE 2: RUN #
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/config-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
