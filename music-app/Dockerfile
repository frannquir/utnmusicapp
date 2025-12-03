FROM amazoncorretto:21

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 10000

CMD java -jar target/*.jar --server.port=10000
