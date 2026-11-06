FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN useradd --system --uid 10001 --create-home appuser \
    && mkdir -p /app/uploads /app/logs \
    && chown -R appuser:appuser /app

COPY --from=build --chown=appuser:appuser /app/target/*.jar /app/app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-jar", "/app/app.jar"]
