# Build from the repository root:
#   docker build -f auth-service/Dockerfile -t secureauth/auth-service .
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl auth-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/auth-service/target/auth-service-2.0.0.jar app.jar
USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
