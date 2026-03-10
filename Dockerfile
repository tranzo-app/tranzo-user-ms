# ============================
# 1️⃣ Build Stage: Compile Java Code
# ============================
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the entire project
COPY . .

# Build the application (JAR file)
RUN mvn clean package -DskipTests

# ============================
# 2️⃣ Run Stage: Use Lightweight Java Runtime
# ============================
FROM eclipse-temurin:21-jre

# Set the working directory
WORKDIR /app

# Copy only the JAR file from the build stage
COPY --from=build /app/target/tranzo-user-ms-0.0.1-SNAPSHOT.jar app.jar

# Run as non-root user
RUN groupadd -r app && useradd -r -g app app
RUN chown -R app:app /app
USER app

# Expose application port
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]