# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy project files and build the Spring Boot jar
# Copy pom.xml and download dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the compiled jar from the build stage
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

# Use the docker profile by default (overridable at runtime)
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
