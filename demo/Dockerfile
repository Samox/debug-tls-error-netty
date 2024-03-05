# Use a base image with Java and Maven installed
FROM maven:3.8.4-openjdk-11-slim AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project descriptor
COPY pom.xml .

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean package

# Create a new image with only the JRE
FROM amazoncorretto:8

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file from the build stage to the current directory
COPY --from=build /app/target/demo-1.0-SNAPSHOT.jar .

# Expose the port your application runs on
EXPOSE 8080

# Command to run your application
CMD ["java", "-jar", "demo-1.0-SNAPSHOT.jar"]
