FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/compliance-service-*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]

