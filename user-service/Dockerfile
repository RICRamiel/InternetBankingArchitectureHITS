# запуск из корня проекта командой
# docker build -f api-gateway/Dockerfile -t api-gateway .
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -pl user-service -am -DskipTests



FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]