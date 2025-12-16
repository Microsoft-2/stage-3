FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/lib /app/lib

COPY --from=build /app/target/search-engine-1.0-SNAPSHOT.jar app.jar

RUN mkdir -p /app/data
ENV BROKER_URL=tcp://activemq:61616
ENV DATALAKE_PATH=/app/data


ENTRYPOINT ["java", "-cp", "app.jar:lib/*"]
