FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /opt/app
COPY --from=build /app/target/performance-service-*.jar app.jar
EXPOSE 8094
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-XX:+UseG1GC","-XX:+UseContainerSupport","-jar","/opt/app/app.jar"]
