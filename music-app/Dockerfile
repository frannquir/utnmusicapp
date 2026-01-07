FROM maven:3.9.9-amazoncorretto-21 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-Xmx350m", "-Dserver.port=${PORT}", "-jar", "app.jar"]