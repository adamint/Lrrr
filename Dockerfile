FROM openjdk:8-jre-slim

COPY ./build/libs/*.jar ./lrrr.jar

ENTRYPOINT ["java", "-jar", "lrrr.jar"]