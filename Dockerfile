FROM amazoncorretto:21-al2-jdk
RUN mkdir /app
WORKDIR /app
COPY target/*.jar /app/cryptomoedas.jar
CMD ["java", "-jar", "/app/cryptomoedas.jar"]
