FROM gradle:8.9-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/build/libs/*.jar app.jar
RUN mkdir -p /app/storage
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=10s --retries=10 --start-period=90s \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=prod"]
