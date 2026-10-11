# Build from the repository root:
#   docker build -f email-service/Dockerfile -t secureauth/email-service .
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl email-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/email-service/target/email-service-2.0.0.jar app.jar
USER app
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
