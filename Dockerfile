FROM maven:3.9.12-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN ./mvnw -B -DskipTests clean install -Pproduction

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} $JAVA_OPTS -jar app.jar"]