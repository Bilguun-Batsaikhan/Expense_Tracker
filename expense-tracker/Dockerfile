# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
#  After this step, the container has a compiled JAR.
RUN mvn -q -DskipTests package 

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Spring profile for docker
# this tells to spring use application-docker.yml 
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080
# When the container starts, java -jar /app/app.jar will be executed
ENTRYPOINT ["java","-jar","/app/app.jar"]
