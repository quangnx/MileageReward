# Use official OpenJDK base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar file (built externally)
COPY build/libs/mileage-job.jar app.jar

# Expose port if needed (optional)
EXPOSE 8080

# Set default command
ENTRYPOINT ["java", "-jar", "app.jar"]