# Use a base image with Java and Maven installed
FROM jelastic/maven:3.9.5-openjdk-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project descriptor
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src
# Build the application
RUN mvn clean package

# Create a new image with only the JRE
FROM amazoncorretto:21

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file from the build stage to the current directory
COPY --from=build /app/target/demo-1.0-SNAPSHOT.jar .

# Expose the port your application runs on
EXPOSE 8080

# Command to run your application
CMD ["java", "-jar", "demo-1.0-SNAPSHOT.jar"]
