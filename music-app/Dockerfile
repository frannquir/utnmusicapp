FROM amazoncorretto:21-alpine-jdk AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

CMD ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]