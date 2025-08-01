FROM openjdk:21-jdk-slim

WORKDIR /app

COPY . .

# Build the application using Maven
RUN ./mvnw clean package -DskipTests

# Expose port 10000 (Render's default port)
EXPOSE 10000

# Run the jar file with the correct port
CMD ["java", "-jar", "target/*.jar", "--server.port=10000"]
