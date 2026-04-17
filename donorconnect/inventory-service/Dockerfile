FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/inventory-service-1.0.0.jar app.jar
EXPOSE 8010
ENTRYPOINT ["java", "-jar", "app.jar"]
