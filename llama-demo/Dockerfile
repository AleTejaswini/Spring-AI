FROM eclipse-temurin:24-jre

WORKDIR /app

COPY target/ollama-demo-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]