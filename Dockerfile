FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the generated JAR file (use the actual name from your build/libs directory)
COPY build/libs/requests-management-0.0.1-SNAPSHOT.jar app.jar

# Copy application properties
COPY src/main/resources/application.yml /app/application.yml

EXPOSE 8081

CMD ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.yml"]