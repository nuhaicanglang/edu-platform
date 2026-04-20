FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/edu-system-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
