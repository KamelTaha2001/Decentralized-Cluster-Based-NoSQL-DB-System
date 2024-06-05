FROM openjdk:19-alpine

WORKDIR /app

COPY /target/EmailDemoApp-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8082

CMD ["java", "-jar", "app.jar"]