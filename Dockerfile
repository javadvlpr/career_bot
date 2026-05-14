# ============ Build stage ============
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and dependency descriptor first (for layer caching)
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x mvnw

# Download dependencies (cached layer if pom.xml hasn't changed)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src src
RUN ./mvnw package -DskipTests -B

# ============ Runtime stage ============
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Railway/Heroku-style port support
EXPOSE 8080

# Set production profile by default; can be overridden by SPRING_PROFILES_ACTIVE env var
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
