# Build the Java application.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp clean package

# Run the application.
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd -r appuser \
    && useradd -r -g appuser -d /app appuser \
    && mkdir -p /app/data \
    && chown -R appuser:appuser /app

COPY --from=build --chown=appuser:appuser \
    /build/target/concurrent-reservation-service-0.0.1-SNAPSHOT.jar \
    /app/app.jar

USER appuser

ENV RESERVATION_STORAGE=file

EXPOSE 8080

# Use the cloud provider's PORT value when available.
CMD ["sh", "-c", "exec java -jar /app/app.jar --server.address=0.0.0.0 --server.port=${PORT:-8080}"]
