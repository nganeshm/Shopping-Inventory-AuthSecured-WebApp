# Build from the repository root:
#   docker build -f customer-service/Dockerfile -t secureauth/customer-service .
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl customer-service -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/customer-service/target/customer-service-2.0.0.jar app.jar
USER app
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
