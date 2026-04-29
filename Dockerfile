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
    wget -q https://github.com/xerial/sqlite-jdbc/releases/download/3.41.2.2/sqlite-jdbc-3.41.2.2.jar && \
    wget -q https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar

# Compile Java files
RUN cd src/main/java && \
    find . -name "*.java" | xargs javac -d /app/target/classes \
    -cp /app/lib/sqlite-jdbc-3.41.2.2.jar:/app/lib/jakarta.servlet-api-6.0.0.jar

# Copy web content
COPY src/main/webapp/ target/webapp/

# Create WAR file - preserving WEB-INF structure
RUN cd target/webapp && \
    mkdir -p WEB-INF/classes && \
    cp -r /app/target/classes/* WEB-INF/classes/ && \
    cp /app/lib/*.jar WEB-INF/lib/ && \
    cd /app/target && \
    jar -cf attendance.war -C webapp .

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

# Start Tomcat
CMD ["/tomcat/bin/catalina.sh", "run"]
