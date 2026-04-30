# Stage 1: Build
FROM eclipse-temurin:11-jdk-alpine as builder

# Cache bust argument to force fresh rebuild
ARG BUILD_ID=default
RUN echo "Building with BUILD_ID=${BUILD_ID}"

WORKDIR /app

# Copy source
COPY src/ src/

# Create directories
RUN mkdir -p target/classes lib target/webapp/WEB-INF/classes target/webapp/WEB-INF/lib

# Download dependencies
RUN cd lib && \
    wget -q https://jdbc.postgresql.org/download/postgresql-42.7.3.jar && \
    wget -q https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar

# Compile Java files
RUN cd src/main/java && \
    find . -name "*.java" | xargs javac -d /app/target/classes \
    -cp /app/lib/postgresql-42.7.3.jar:/app/lib/jakarta.servlet-api-6.0.0.jar

# Copy web content
COPY src/main/webapp/ target/webapp/

# Create WAR file - preserving WEB-INF structure
RUN cd target/webapp && \
    mkdir -p WEB-INF/classes && \
    cp -r /app/target/classes/* WEB-INF/classes/ && \
    cp /app/lib/postgresql-*.jar WEB-INF/lib/ && \
    cd /app/target && \
    jar -cf attendance.war -C webapp .

# Stage 2: Runtime
FROM eclipse-temurin:11-jre-alpine

# Install Tomcat 10 (which supports jakarta.servlet)
RUN apk add --no-cache wget bash && \
    wget -q https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.20/bin/apache-tomcat-10.1.20.tar.gz && \
    tar -xzf apache-tomcat-10.1.20.tar.gz && \
    rm apache-tomcat-10.1.20.tar.gz && \
    mv apache-tomcat-10.1.20 /tomcat

# Remove default webapps
RUN rm -rf /tomcat/webapps/*

# Copy built WAR from builder stage
COPY --from=builder /app/target/attendance.war /tomcat/webapps/attendance.war

# Expose port 8080
EXPOSE 8080

# Start Tomcat
CMD ["/tomcat/bin/catalina.sh", "run"]
