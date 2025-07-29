FROM openjdk:21-jdk

WORKDIR /app

COPY target/*.jar app.jar

# Expose port (optional, adjust if needed)
EXPOSE 9094

# Run the Spring Boot app with the active profile
ENTRYPOINT ["java", "-jar", "app.jar"]