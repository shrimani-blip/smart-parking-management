FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .

RUN mkdir -p src/main/java/com/college/smartparking
RUN mkdir -p src/main/resources

COPY *.java src/main/java/com/college/smartparking/
COPY application.properties src/main/resources/

RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/smart-parking-web-1.0.0.jar app.jar

ENV PORT=10000
EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]
