FROM openjdk17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} studylog.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "studylog.jar"]
