# 后端生产镜像

FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /app/target/Intelligent-interview-question-bank-system-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8101

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
