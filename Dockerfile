FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S ares && adduser -S ares -G ares
WORKDIR /app
COPY --from=build /workspace/target/ares-api-*.jar app.jar
USER ares
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
