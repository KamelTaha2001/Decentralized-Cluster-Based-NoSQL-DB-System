FROM openjdk:19-alpine

WORKDIR /app

COPY /target/BootstrapNode-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]