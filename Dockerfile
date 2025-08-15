# build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY gradlew gradle/ build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --version
COPY src src
RUN ./gradlew bootJar --no-daemon

# run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
