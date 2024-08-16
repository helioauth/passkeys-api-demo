# Use the official gradle image as the base image
FROM maven:3.9-eclipse-temurin-22-jammy AS _build

# Set the working directory in the container
WORKDIR /app

# Copy the build files to the container
COPY ./build.gradle .
COPY ./src ./src

# Build the application using Gradle
RUN ./mvnw package

# Create a new stage for the final image
FROM eclipse-temurin:22-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the previous stage
COPY --from=_build_ /app/target/*.jar /app/

# Configure the container to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]