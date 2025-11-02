FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/brokerage-service-0.0.1-SNAPSHOT.jar app.jar

#default port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]