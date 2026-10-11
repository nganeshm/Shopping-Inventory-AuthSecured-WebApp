# Build from the repository root:
#   docker build -f eureka-server/Dockerfile -t secureauth/eureka-server .
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl eureka-server -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/eureka-server/target/eureka-server-2.0.0.jar app.jar
USER app
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
