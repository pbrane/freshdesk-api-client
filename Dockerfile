# Set the base image
FROM eclipse-temurin:21-jdk-noble AS builder

# Set the working directory
WORKDIR /app

# Copy Maven wrapper and configuration
COPY mvnw .
COPY .mvn .mvn

# Copy the custom dependency JAR file
COPY lib/tac-case-api-service-monolith-0.1.16-SNAPSHOT.jar /app/lib/

# Ensure the Maven wrapper script is executable
RUN chmod +x mvnw

# Install the custom dependency into the default Maven local repository
RUN ./mvnw install:install-file \
    -Dfile=/app/lib/tac-case-api-service-monolith-0.1.16-SNAPSHOT.jar \
    -DgroupId=com.beaconstrategists \
    -DartifactId=tac-case-api-service-monolith \
    -Dversion=0.1.16-SNAPSHOT \
    -Dpackaging=jar

# Copy the project files
COPY pom.xml .
COPY src ./src

# Ensure the Maven wrapper script is executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests -P container-build
#RUN ./mvnw clean package -Pcontainer-build -DskipTests \
#    -Dcustom.repo.file=./lib/tac-case-api-service-monolith-0.1.16-SNAPSHOT.jar

# Final image
FROM eclipse-temurin:21-jdk-noble

# Install handy utilites and clean up apt cache
RUN apt-get update && apt-get install -y reptyr dnsutils bind9-utils net-tools iproute2 iputils-ping iputils-tracepath ncat && apt-get clean && rm -rf /var/lib/apt/lists/*

#The Maven container-build profile sets the final name of the JAR to "app.jar"
COPY --from=builder /app/target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
