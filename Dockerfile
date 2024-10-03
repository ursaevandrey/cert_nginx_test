FROM openjdk:23

WORKDIR /app
COPY dist/* .

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app-release.jar"]
