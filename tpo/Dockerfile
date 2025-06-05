FROM openjdk:21-jdk-slim
ARG JAR_FILE=target/tpo-0.0.1.jar
COPY ${JAR_FILE} app_TurnosYa.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app_TurnosYa.jar"]