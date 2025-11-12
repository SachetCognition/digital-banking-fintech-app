FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/admin-service-*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]

