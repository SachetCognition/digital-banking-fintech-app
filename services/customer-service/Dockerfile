# Build stage
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /opt/app
COPY --from=build /app/target/customer-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
