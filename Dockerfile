# Step 1: Use an official lightweight OpenJDK image as the base
FROM eclipse-temurin:17-jre-alpine

# Step 2: Set the working directory inside the container
WORKDIR /app

# Step 3: Copy the compiled JAR file from your target folder into the container
# Note: We assume your jar file is named based on your project. We can use a wildcard.
COPY target/*.jar app.jar

# Step 4: Expose port 8080 so we can access the backend
EXPOSE 8080

# Step 5: Command to execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]