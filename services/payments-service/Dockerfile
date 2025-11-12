FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/payments-service-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]

