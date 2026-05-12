FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy any jar from target folder
COPY target/*.jar app.jar

# Expose port (optional, overridden by docker-compose)
EXPOSE 8088

ENTRYPOINT ["java","-jar","app.jar"]