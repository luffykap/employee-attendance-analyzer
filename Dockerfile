# Stage 1: Build
FROM eclipse-temurin:11-jdk-alpine as builder

WORKDIR /app

# Copy source and build scripts
COPY src/ src/
COPY build.sh .
COPY target/ target/

# Create lib directory
RUN mkdir -p lib

# Run build (will download dependencies)
RUN chmod +x build.sh && ./build.sh

# Stage 2: Runtime
FROM eclipse-temurin:11-jre-alpine

# Install Tomcat
RUN apk add --no-cache wget bash && \
    wget -q https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.88/bin/apache-tomcat-9.0.88.tar.gz && \
    tar -xzf apache-tomcat-9.0.88.tar.gz && \
    rm apache-tomcat-9.0.88.tar.gz && \
    mv apache-tomcat-9.0.88 /tomcat

# Remove default webapps
RUN rm -rf /tomcat/webapps/*

# Copy built WAR from builder stage
COPY --from=builder /app/target/attendance.war /tomcat/webapps/attendance.war

# Expose port 8080
EXPOSE 8080

# Set Render port
ENV PORT=8080

# Start Tomcat
CMD ["/tomcat/bin/catalina.sh", "run"]
