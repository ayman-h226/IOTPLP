FROM maven:3.9-eclipse-temurin-17
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mvn clean package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/app.jar"]

