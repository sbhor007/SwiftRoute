# Stage 1: Build

FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and config first (for better caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests


# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

RUN chown -R spring:spring /app
USER spring

EXPOSE 8080

ENTRYPOINT ["java","--enable-preview","-jar","app.jar"]
