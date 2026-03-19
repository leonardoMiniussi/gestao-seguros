FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY target/gestao-seguros-1.0.0.jar ./app.jar

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

