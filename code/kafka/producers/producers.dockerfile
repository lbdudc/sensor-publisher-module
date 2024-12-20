/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Producers) { %*/
FROM openjdk:11

# Set Gradle version and download URL
ENV GRADLE_VERSION=7.4.2
ENV GRADLE_URL=https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip

# Create directory for the Gradle installation
RUN mkdir -p /opt/gradle

# Download and extract Gradle
RUN wget -q --show-progress --progress=bar:force:noscroll -O gradle.zip $GRADLE_URL && \
    unzip -q gradle.zip -d /opt/gradle && \
    rm gradle.zip

# Set Gradle home environment variable
ENV GRADLE_HOME /opt/gradle/gradle-${GRADLE_VERSION}

# Add Gradle binaries to the PATH
ENV PATH $PATH:$GRADLE_HOME/bin

# Create application directory
RUN mkdir -p /app/kafkaproducers

# Set the working directory
WORKDIR /app/kafkaproducers

# Copy Gradle project files including the wrapper script and wrapper properties
COPY . .

# Run the Gradle wrapper to download the Gradle distribution and create the wrapper JAR
RUN /opt/gradle/gradle-${GRADLE_VERSION}/bin/gradle wrapper --gradle-version ${GRADLE_VERSION}

# # Build and run the Spring Boot application with Gradle wrapper
# RUN /opt/gradle/gradle-${GRADLE_VERSION}/bin/gradle build

CMD chmod +x gradlew && ./gradlew bootRun --args='--spring.profiles.active=prod'
/*% } %*/
